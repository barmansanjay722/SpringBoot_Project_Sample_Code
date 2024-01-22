package com.algotic.services.impl;

import com.algotic.base.BrokerAdapterFactory;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticProductCode;
import com.algotic.constants.BrokerEnum;
import com.algotic.constants.OrderTriggerType;
import com.algotic.constants.PlaceOrder;
import com.algotic.constants.PriceType;
import com.algotic.constants.Retention;
import com.algotic.constants.TradeType;
import com.algotic.constants.TransactionType;
import com.algotic.constants.aliceblue.Complexity;
import com.algotic.data.entities.Orders;
import com.algotic.data.entities.PaperOrders;
import com.algotic.data.repositories.BrokerCustomerDetailsRepo;
import com.algotic.data.repositories.BrokerSessionDetailsRepo;
import com.algotic.data.repositories.BrokersRepo;
import com.algotic.data.repositories.OrdersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.exception.ErrorCode;
import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.AlgoticModifyRequest;
import com.algotic.model.request.AlgoticSquareOffRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.response.AlgoticCancelOrderResponse;
import com.algotic.model.response.AlgoticSquareOffResponse;
import com.algotic.model.response.BookPositionResponse;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.HoldingAlgoticResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.OrderResponse;
import com.algotic.model.response.PortfolioResponse;
import com.algotic.model.response.aliceblue.ModifyOrderResponse;
import com.algotic.services.OrderService;
import com.algotic.services.PaperOrderService;
import com.algotic.utils.AlgoticUtils;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private BrokerCustomerDetailsRepo brokerCustomerDetailsRepo;

    @Autowired
    private BrokerSessionDetailsRepo brokerSessionDetailsRepo;

    @Autowired
    private PaperOrderService paperOrderService;

    @Autowired
    private OrdersRepo ordersRepo;

    @Autowired
    private BrokerAdapterFactory adapterFactory;

    @Autowired
    private BrokersRepo brokerRepo;

    @Override
    public ResponseEntity<List<OrderAndTradeBookResponse>> orderBook(String type) {
        try {
            String userId = jwtHelper.getUserId();
            String token = aliceBlueToken(userId);
            List<OrderAndTradeBookResponse> orderResponse = new ArrayList<>();

            addPaperOrderData(orderResponse, userId, type);
            BrokerEnum brokerName =
                    BrokerEnum.getBrokerEnum(brokerRepo.findBrokerNameByCustomerId(jwtHelper.getUserId()));
            if (!StringUtils.isEmpty(token)) {
                if ("tradeBook".equalsIgnoreCase(type)) {
                    addBrokerTradeBookData(orderResponse, brokerName);
                } else {
                    addOrderBookData(orderResponse, brokerName);
                }
                validateOrderResponse(orderResponse, type);
            }
            Comparator<OrderAndTradeBookResponse> timeComparator =
                    Comparator.comparing(obj -> LocalTime.parse(obj.getTime()));
            orderResponse.sort(timeComparator.reversed());

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Order Book",
                            AlgoticUtils.objectToJsonString(orderResponse),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));

            return new ResponseEntity<>(orderResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw new AlgoticException(ex.getErrorCode());
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    private void addPaperOrderData(List<OrderAndTradeBookResponse> orderResponse, String userId, String type) {
        List<OrderAndTradeBookResponse> paperOrder = paperOrderData(userId, type);
        if (paperOrder != null) {
            orderResponse.addAll(paperOrder);
        }
    }

    private void addBrokerTradeBookData(List<OrderAndTradeBookResponse> orderResponse, BrokerEnum brokerName) {

        List<OrderAndTradeBookResponse> orderResponseBroker = tradeBookResponses(brokerName);
        log.debug("Found trade book of size {} from {} broker", orderResponseBroker.size(), brokerName);
        if (!CollectionUtils.isEmpty(orderResponseBroker)) {
            orderResponse.addAll(orderResponseBroker);
        }
    }

    private void addOrderBookData(List<OrderAndTradeBookResponse> orderAndTradeBookResponses, BrokerEnum brokerName) {
        List<OrderAndTradeBookResponse> orderBooks = orderBookResponses(brokerName);
        orderAndTradeBookResponses.addAll(orderBooks);
    }

    private void validateOrderResponse(List<OrderAndTradeBookResponse> orderResponse, String type) {
        if (orderResponse.isEmpty()) {
            CommonErrorCode errorCode = CommonErrorCode.DATA_NOT_FOUND;
            handleErrorResponse(errorCode);
        }
        if ("executed".equalsIgnoreCase(type)) {
            orderResponse.removeIf(o -> o.getStatus().equalsIgnoreCase("open")
                    || o.getStatus().toLowerCase().contains("pending"));
            if (orderResponse.isEmpty()) {
                handleErrorResponse(CommonErrorCode.DATA_NOT_FOUND);
            }
        } else if ("pending".equalsIgnoreCase(type)) {
            orderResponse.removeIf(o -> o.getStatus().equalsIgnoreCase("complete")
                    || o.getStatus().toLowerCase().contains("rejected")
                    || o.getStatus().toLowerCase().contains("cancelled"));
            if (orderResponse.isEmpty()) {
                handleErrorResponse(CommonErrorCode.DATA_NOT_FOUND);
            }
        }
    }

    private List<OrderAndTradeBookResponse> paperOrderData(String userId, String type) {
        try {
            List<OrderAndTradeBookResponse> paperOrder = paperOrderService.paperOrderBook(userId, type);
            return paperOrder;
        } catch (Exception ex) {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("No paper order found", ex.getMessage(), jwtHelper.getUserId(), null));
            return null;
        }
    }

    private List<OrderAndTradeBookResponse> orderBookResponses(BrokerEnum brokerName) {
        try {
            log.info("Invoking {} adapter to get order book", brokerName);
            return adapterFactory.getAdapter(brokerName).getOrderBook(jwtHelper.getUserId());
        } catch (AlgoticException ex) {
            log.error("Some exception occurred while getting order book + returning an empty list", ex);
            return Collections.emptyList();
        }
    }

    private List<OrderAndTradeBookResponse> tradeBookResponses(BrokerEnum brokerName) {
        try {
            log.info("Method start -> tradeBookResponses calling {} adapter to fetch users trade ", brokerName);
            return adapterFactory.getAdapter(brokerName).getTradeBook();
        } catch (AlgoticException ex) {
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            ex.getMessage(),
                            "Exception occurred fetching user trade book...",
                            jwtHelper.getUserId(),
                            null));
            return Collections.emptyList();
        }
    }

    private void handleErrorResponse(CommonErrorCode errorCode) throws AlgoticException {
        log.error(logConfig
                .getLogHandler()
                .getErrorLog(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        jwtHelper.getUserId(),
                        String.valueOf(errorCode.getHttpStatus().value())));
        throw new AlgoticException(errorCode);
    }

    @Override
    public ResponseEntity<List<BookPositionResponse>> getPositions() {
        log.info("Method start -> getPositions()");
        try {

            List<BookPositionResponse> bookPositionResponseList = new ArrayList<>();
            List<BookPositionResponse> paperOrderPositionList = paperOrderPostions();
            if (paperOrderPositionList != null) {
                bookPositionResponseList.addAll(paperOrderPositionList);
            }

            List<BookPositionResponse> liveOrderPositionList = liveOrderPositions();
            bookPositionResponseList.addAll(liveOrderPositionList);

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Position book ",
                            AlgoticUtils.objectToJsonString(bookPositionResponseList),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            bookPositionResponseList.sort(Comparator.comparing(BookPositionResponse::getQuantity, (s1, s2) -> {
                // Compare positive/negative values first
                int quantity1 = Integer.parseInt(s1);
                int quantity2 = Integer.parseInt(s2);
                if (quantity1 != 0 && quantity2 != 0) {
                    return Integer.compare(quantity2, quantity1);
                }

                // Move all non-zero values to the top
                if (quantity1 != 0) {
                    return -1;
                } else if (quantity2 != 0) {
                    return 1;
                }

                return 0;
            }));

            return new ResponseEntity<>(bookPositionResponseList, HttpStatus.OK);
        } catch (AlgoticException ex) {
            log.error("Algotic exception occur while geting positions data -> {}", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occur while geting positions data -> {}", ex);
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method is used for getting broker position
     * As per broker name it fetch their respective position data
     */
    private List<BookPositionResponse> liveOrderPositions() {
        try {
            log.info("Feching live order position for broker.");
            String userId = jwtHelper.getUserId();
            String brokerName = brokerRepo.findBrokerNameByCustomerId(userId);
            return adapterFactory
                    .getAdapter(BrokerEnum.getBrokerEnum(brokerName))
                    .bookPosition(userId);

        } catch (AlgoticException e) {
            log.error("Algotic exception occure while getting live position data -> {}", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Exception occure while getting live position data -> {}", e);
            return Collections.emptyList();
        }
    }

    private List<BookPositionResponse> paperOrderPostions() {
        try {
            return paperOrderService.positionBook(jwtHelper.getUserId());
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *This method is used for getting both paper holdings and live holdings
     */
    @Override
    public ResponseEntity<List<HoldingAlgoticResponse>> getHoldings() {
        log.info("Method start -> getHoldings()");
        try {
            List<HoldingAlgoticResponse> holdingResponseList = new ArrayList<>();
            List<HoldingAlgoticResponse> paperHoldingList = getPaperHoldings();
            if (paperHoldingList != null) {
                holdingResponseList.addAll(paperHoldingList);
            }

            List<HoldingAlgoticResponse> liveHoldingList = getLiveHoldings();
            holdingResponseList.addAll(liveHoldingList);

            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Holdings Order",
                            AlgoticUtils.objectToJsonString(holdingResponseList),
                            jwtHelper.getUserId(),
                            String.valueOf(HttpStatus.OK.value())));
            return new ResponseEntity<>(holdingResponseList, HttpStatus.OK);
        } catch (AlgoticException ex) {
            log.error("Algotic exception occur while geting holding data -> {}", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occur while geting holding data -> {}", ex);
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This method used to fetch live holding data
     * It returns their respective holdings as per the name of the broker
     * @return HoldingAlgoticResponse
     * @throws NoSuchAlgorithmException
     */
    private List<HoldingAlgoticResponse> getLiveHoldings() throws NoSuchAlgorithmException {
        try {
            log.info("Fetching live holdings for broker.");
            String userId = jwtHelper.getUserId();
            String brokerName = brokerRepo.findBrokerNameByCustomerId(userId);
            return adapterFactory
                    .getAdapter(BrokerEnum.getBrokerEnum(brokerName))
                    .holdings(userId);

        } catch (AlgoticException ex) {
            log.error("Algotic exception occure while getting live holding data -> {}", ex);
            return Collections.emptyList();
        } catch (Exception e) {
            log.info("Exception occure while getting live holdings data -> {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<HoldingAlgoticResponse> getPaperHoldings() {
        return paperOrderService.holdingsPaperTrade(jwtHelper.getUserId());
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> placeOrder(OrderRequest orderRequest) {
        log.info("Method start to place an order");

        Orders orders = saveOrder(orderRequest);

        if (orderRequest.getTradeType().equalsIgnoreCase(TradeType.LIVE.name())) {
            log.info("Trade Type is Live");
            String userID = jwtHelper.getUserId();
            log.info("Fetching broker name for user {}", userID);
            String brokerName = brokerRepo.findBrokerNameByCustomerId(userID);
            placeLiveOrder(orderRequest, orders, BrokerEnum.getBrokerEnum(brokerName), userID);
        } else if (orderRequest.getTradeType().equalsIgnoreCase(TradeType.PAPER.name())
                && orderRequest.getPriceType().equalsIgnoreCase(PriceType.MARKET.name())) {
            log.info("Trade Type is Paper");
            placePaperOrder(orderRequest, orders);
        } else {
            log.info("Invalid Trade type it should be either Live or paper");
            throw new AlgoticException(BusinessErrorCode.TRADE_TYPE_NOT_EXISTS);
        }

        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Place order",
                        "Order placed successfully",
                        jwtHelper.getUserId(),
                        String.valueOf(HttpStatus.CREATED.value())));

        return new ResponseEntity<>(new GlobalMessageResponse("Order placed successfully"), HttpStatus.CREATED);
    }

    /**
     * Method responsible to placing an order i.e. (buy/sell) using broker api, it
     * is either Alice blue or motilal
     *
     * @param orderRequest
     * @param orders
     * @param brokerName
     * @param userId
     */
    private void placeLiveOrder(OrderRequest orderRequest, Orders orders, BrokerEnum brokerName, String userId) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Method start -> placeLiveOrder", brokerName.getBrokerName(), userId, null));

        if (orderRequest.getPriceType().equalsIgnoreCase("MKT")
                || orderRequest.getPriceType().equalsIgnoreCase("MARKET")) {
            orderRequest.setPrice(null);
        }

        OrderResponse orderData = adapterFactory.getAdapter(brokerName).placeOrder(orderRequest, userId);
        Integer brokerId = brokerRepo.findbrokerId(brokerName.getBrokerName());
        orders.setNestOrderNumber(orderData.getOrderRefNumber());
        orders.setBrokerId(brokerId);
        orders.setStatus(orderData.getOrderStatus());
        orders.setModifiedAt(new Date());
        orders.setBrokerId(brokerId);

        if (orderData.getErrorMessage() != null) {
            orders.setErrorMessage(orderData.getErrorMessage());
        } else {
            orders.setErrorMessage(orderData.getErrorMsge());
        }

        log.info("Updating order details into DB...");
        ordersRepo.save(orders);

        if (orderData.isException()) {
            log.error(
                    "Exception occurred from {} broker while placing an order {}",
                    brokerName,
                    orderData.getErrorCode());
            throw new AlgoticException(orderData.getErrorCode());
        }
    }

    private void placePaperOrder(OrderRequest orderRequest, Orders orders) {
        try {
            PaperOrders paperOrders = paperOrderService.paperOrder(orderRequest, null);
            orders.setNestOrderNumber(paperOrders.getId());
            orders.setStatus(PlaceOrder.PLACED.name());
            orders.setTradeType(TradeType.PAPER.name());
            orders.setModifiedAt(new Date());

            ordersRepo.save(orders);
            log.info("Successfully placed paper order..{}", orders.getTradeId());
        } catch (Exception e) {
            log.error("Exception ocurred while placing paperorder", e);
            throw new AlgoticException(CommonErrorCode.PAPER_TRADE_ERROR);
        }
    }

    private Orders saveOrder(OrderRequest orderRequest) {
        try {
            String tradeId = AlgoticUtils.generateUUID();
            Orders orders = new Orders();
            orders.setComplexity(Complexity.REGULAR.name());
            orders.setTradingSymbol(orderRequest.getTradingSymbol());
            orders.setTransactionType(orderRequest.getTransactionType());
            orders.setTriggerPrice(orderRequest.getTriggerPrice());
            orders.setRetention(Retention.DAY.name());
            orders.setQuantity(orderRequest.getQuantity());
            orders.setPrice(orderRequest.getPrice());
            orders.setPriceType(orderRequest.getPriceType());
            orders.setProductCode(orderRequest.getProductCode());
            orders.setExchange(orderRequest.getExchange());
            orders.setDiscloseQuantity(0);
            orders.setUserId(jwtHelper.getUserId());
            orders.setNestOrderNumber(null);
            orders.setTradeId(tradeId);
            orders.setTradeType(TradeType.LIVE.name());
            orders.setOrderType(OrderTriggerType.MANUAL.name());
            orders.setStatus(PlaceOrder.CREATED.name());
            orders.setToken(orderRequest.getToken());
            orders.setCreatedAt(new Date());
            ordersRepo.save(orders);
            return orders;
        } catch (Exception ex) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<AlgoticCancelOrderResponse> cancelOrder(AlgoticCancelOrderRequest algoticRequest) {
        try {
            String userId = jwtHelper.getUserId();
            String brokerName = brokerRepo.findBrokerNameByCustomerId(userId);
            AlgoticCancelOrderResponse cancelOrderResponse = adapterFactory
                    .getAdapter(BrokerEnum.getBrokerEnum(brokerName))
                    .cancelOrder(algoticRequest, userId);

            if (cancelOrderResponse.isException()) {
                log.error(
                        "Exception occurred from {} broker while canceling an order {}",
                        brokerName,
                        cancelOrderResponse.getErrorode());
                throw new AlgoticException(cancelOrderResponse.getErrorode());
            }
            logCancellation(userId, cancelOrderResponse);
            return new ResponseEntity<>(cancelOrderResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception ex) {
            throw handleCancellationError(jwtHelper.getUserId(), null);
        }
    }

    private AlgoticException handleCancellationError(String userId, ErrorCode errorCode) {
        ErrorCode cancelErrorCode = errorCode != null ? errorCode : CommonErrorCode.INTERNAL_SERVER_ERROR;
        log.error(logConfig
                .getLogHandler()
                .getErrorLog(
                        cancelErrorCode.getErrorCode(),
                        cancelErrorCode.getErrorMessage(),
                        userId,
                        String.valueOf(cancelErrorCode.getHttpStatus().value())));
        return new AlgoticException(cancelErrorCode);
    }

    private void logCancellation(String userId, AlgoticCancelOrderResponse orderResponse) {
        log.error(logConfig
                .getLogHandler()
                .getErrorLog(
                        "Cancel Order",
                        AlgoticUtils.objectToJsonString(orderResponse),
                        userId,
                        String.valueOf(HttpStatus.OK.value())));
    }

    /**
     *Method used for doing square off all open positions
     *It square Off both paper and live open positions
     */
    @Override
    public ResponseEntity<AlgoticSquareOffResponse> squareOffAll() {
        try {
            log.info("Method start -> squareOffAll()");
            AlgoticSquareOffResponse squareOffResponse = new AlgoticSquareOffResponse();

            log.info("Square Off of paper Data");
            AlgoticSquareOffResponse paperOrderSquareoff = paperSquareOffAll();
            if (paperOrderSquareoff != null) {
                squareOffResponse.setMessage(paperOrderSquareoff.getMessage());
                squareOffResponse.setStatus(paperOrderSquareoff.getStatus());
            }

            log.info("Fetching broker name for doing squreOffAll");
            BrokerEnum brokerName =
                    BrokerEnum.getBrokerEnum(brokerRepo.findBrokerNameByCustomerId(jwtHelper.getUserId()));
            if (brokerName != null) {
                log.info("Start doing squareOffAll for broker -> {}", brokerName);
                AlgoticSquareOffResponse liveSquareOffResponse = liveSqureOffAll(brokerName);
                if (liveSquareOffResponse != null) {
                    squareOffResponse.setMessage(liveSquareOffResponse.getMessage());
                    squareOffResponse.setStatus(liveSquareOffResponse.getStatus());
                }
            }

            logSquareOffInfo(squareOffResponse);

            return new ResponseEntity<>(squareOffResponse, HttpStatus.OK);
        } catch (AlgoticException e) {
            log.error("Algotic exception occure while doing squareOffAll -> {} ", e);
            throw e;

        } catch (Exception e) {
            log.error("Exception occure while doing squareOffAll -> {}", e.getMessage());
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private AlgoticSquareOffResponse liveSqureOffAll(BrokerEnum brokerName) {
        try {
            return adapterFactory.getAdapter(brokerName).squareOffAll();
        } catch (Exception e) {
            return null;
        }
    }

    private AlgoticSquareOffResponse paperSquareOffAll() {
        try {
            return paperOrderService.paperOrderSquareOffAll();
        } catch (Exception e) {
            return null;
        }
    }

    private void logSquareOffInfo(AlgoticSquareOffResponse squareOffResponse) {
        String userId = jwtHelper.getUserId();
        HttpStatus httpStatus = HttpStatus.OK;

        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Square Off ",
                        AlgoticUtils.objectToJsonString(squareOffResponse),
                        userId,
                        String.valueOf(httpStatus.value())));
    }

    @Override
    public ResponseEntity<GlobalMessageResponse> modifyOrder(AlgoticModifyRequest modifyRequest) {
        validateModifyRequest(modifyRequest);
        String userId = jwtHelper.getUserId();
        String brokerName = brokerRepo.findBrokerNameByCustomerId(userId);
        ModifyOrderResponse modifyOrderResponse =
                adapterFactory.getAdapter(BrokerEnum.getBrokerEnum(brokerName)).modifyOrder(modifyRequest);
        modifyOrderResponse.setStatus(PlaceOrder.MODIFIED.name());
        log.error(logConfig
                .getLogHandler()
                .getErrorLog(
                        "Modify order ", "Order Modify successfully", userId, String.valueOf(HttpStatus.OK.value())));
        return ResponseEntity.ok(new GlobalMessageResponse("Order modify successfully"));
    }

    private void validateModifyRequest(AlgoticModifyRequest modifyRequest) {
        validateEnum(modifyRequest.getPriceType(), PriceType.class, CommonErrorCode.INVALID_PRICE_TYPE);
        validateEnum(modifyRequest.getProductCode(), AlgoticProductCode.class, CommonErrorCode.INVALID_PRODUCT_CODE);
        validateEnum(
                modifyRequest.getTransactionType(), TransactionType.class, CommonErrorCode.INVALID_TRANSACTION_TYPE);
    }

    private <T extends Enum<T>> void validateEnum(String value, Class<T> enumClass, CommonErrorCode errorCode) {
        if (!EnumUtils.isValidEnum(enumClass, value)) {
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            errorCode.getErrorCode(),
                            errorCode.getErrorMessage(),
                            jwtHelper.getUserId(),
                            String.valueOf(errorCode.getHttpStatus().value())));
            throw new AlgoticException(errorCode);
        }
    }

    /**
     *Fetching portfolio data both paper and live
     */
    @Override
    public ResponseEntity<PortfolioResponse> getPortfolioData() {
        try {

            Double totalLiveInvestment = 0.0;
            Double toatalPaperInvestment = 0.0;
            List<HoldingAlgoticResponse> holdings = new ArrayList<>();

            List<HoldingAlgoticResponse> paperHoldingResponse = getPaperHoldings();
            if (paperHoldingResponse != null) {
                for (HoldingAlgoticResponse holdingValue : paperHoldingResponse) {
                    double price = holdingValue.getPrice().equalsIgnoreCase("null")
                            ? 0
                            : Double.parseDouble(holdingValue.getPrice());
                    double quantity = holdingValue.getQuantity().equalsIgnoreCase("null")
                            ? 0
                            : Double.parseDouble(holdingValue.getQuantity());
                    Double totalValue = price * quantity;
                    toatalPaperInvestment += totalValue;
                    holdings.add(holdingValue);
                }
            }

            List<HoldingAlgoticResponse> holdingData = getLiveHoldings();
            if (!holdingData.isEmpty()) {
                for (HoldingAlgoticResponse holdingValue : holdingData) {
                    double sellableQty = Double.parseDouble(holdingValue.getQuantity());
                    double price = Double.parseDouble(holdingValue.getPrice());
                    totalLiveInvestment += sellableQty * price;

                    holdings.add(holdingValue);
                }
            }

            PortfolioResponse portfolioResponse = new PortfolioResponse();
            DecimalFormat df = new DecimalFormat("####0.00");
            if (paperHoldingResponse != null) {
                setPortfolioResponseHoldings(portfolioResponse, paperHoldingResponse);
            }
            portfolioResponse.setTotalPaperInvestment(Double.parseDouble(df.format(toatalPaperInvestment)));
            portfolioResponse.setTotalLiveInvestment(Double.parseDouble(df.format(totalLiveInvestment)));
            portfolioResponse.setHoldings(holdings);
            logPortfolioInfo(portfolioResponse);
            return new ResponseEntity<>(portfolioResponse, HttpStatus.OK);

        } catch (AlgoticException ex) {
            log.error("Some error occure while getting portfolio data -> {}", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Exception occure while geeting portfolio data -> {}", ex.getMessage());
            throw handleInternalServerError();
        }
    }

    private void setPortfolioResponseHoldings(
            PortfolioResponse portfolioResponse, List<HoldingAlgoticResponse> paperHoldingResponse) {
        if (!paperHoldingResponse.isEmpty()) {
            portfolioResponse.setHoldings(paperHoldingResponse);
        } else {
            portfolioResponse.setHoldings(new ArrayList<>());
        }
    }

    private AlgoticException handleInternalServerError() {
        CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        logError(errorCode);
        return new AlgoticException(errorCode);
    }

    private void logPortfolioInfo(PortfolioResponse portfolioResponse) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Total investment ",
                        AlgoticUtils.objectToJsonString(portfolioResponse),
                        jwtHelper.getUserId(),
                        String.valueOf(HttpStatus.OK.value())));
    }

    private void logError(CommonErrorCode errorCode) {
        log.error(logConfig
                .getLogHandler()
                .getErrorLog(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        jwtHelper.getUserId(),
                        String.valueOf(errorCode.getHttpStatus().value())));
    }

    /**
     *This method is used for doing squareoff of individual open position for both paper and live open position
     */
    @Override
    public ResponseEntity<AlgoticSquareOffResponse> squareOff(AlgoticSquareOffRequest algoticSquareOffRequest) {
        log.info("Service method start -> squareOff()");
        try {
            AlgoticSquareOffResponse algoticSquareOffResponse = new AlgoticSquareOffResponse();
            log.info("Method start for squreOff of paper data");
            AlgoticSquareOffResponse paperOrderSquareOff = paperSquareOff(
                    String.valueOf(algoticSquareOffRequest.getToken()),
                    algoticSquareOffRequest.getProductCode(),
                    algoticSquareOffRequest.getPrice(),
                    jwtHelper.getUserId());
            if (paperOrderSquareOff != null) {
                algoticSquareOffResponse.setMessage(paperOrderSquareOff.getMessage());
                algoticSquareOffResponse.setStatus(paperOrderSquareOff.getStatus());
            }
            if (algoticSquareOffRequest.getTradeType().equalsIgnoreCase(TradeType.LIVE.name())) {
                log.info("SquareOff of live Position data.");
                AlgoticSquareOffResponse liveOrderSqureOffResponse = liveSquareOff(algoticSquareOffRequest);

                algoticSquareOffResponse.setMessage(liveOrderSqureOffResponse.getMessage());
                algoticSquareOffResponse.setStatus(liveOrderSqureOffResponse.getStatus());
            }

            logSquareOffOrder(algoticSquareOffResponse);
            return new ResponseEntity<>(algoticSquareOffResponse, HttpStatus.OK);
        } catch (AlgoticException ex) {
            log.error("Algotic exception occur doing squreOff for individual open positions data -> {}", ex);
            throw ex;
        } catch (Exception ex) {
            CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
            logInternalError(errorCode);
            throw new AlgoticException(errorCode);
        }
    }

    private AlgoticSquareOffResponse liveSquareOff(AlgoticSquareOffRequest algoticSquareOffRequest) {
        try {
            log.info("Method start -> liveSquareOff()");
            BrokerEnum brokerName =
                    BrokerEnum.getBrokerEnum(brokerRepo.findBrokerNameByCustomerId(jwtHelper.getUserId()));
            return adapterFactory.getAdapter(brokerName).squareOff(algoticSquareOffRequest);
        } catch (Exception e) {
            return new AlgoticSquareOffResponse("Failed", "Error occure while doing live squre off");
        }
    }

    private AlgoticSquareOffResponse paperSquareOff(String token, String productCode, Double price, String userId) {
        log.info("Start doing paperSquareOff");
        try {
            AlgoticSquareOffResponse algoticSquareOffResponse = new AlgoticSquareOffResponse();
            AlgoticSquareOffResponse paperOrderSquareOff =
                    paperOrderService.paperOrderSquareOff(token, productCode, price, userId);
            algoticSquareOffResponse.setMessage(paperOrderSquareOff.getMessage());
            algoticSquareOffResponse.setStatus(paperOrderSquareOff.getStatus());
            return algoticSquareOffResponse;
        } catch (Exception e) {
            return null;
        }
    }

    private void logSquareOffOrder(AlgoticSquareOffResponse algoticSquareOffResponse) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "SquareOff Order",
                        AlgoticUtils.objectToJsonString(algoticSquareOffResponse),
                        jwtHelper.getUserId(),
                        String.valueOf(HttpStatus.OK.value())));
    }

    private void logInternalError(CommonErrorCode errorCode) {
        log.error(logConfig
                .getLogHandler()
                .getErrorLog(
                        errorCode.getErrorCode(),
                        errorCode.getErrorMessage(),
                        jwtHelper.getUserId(),
                        String.valueOf(errorCode.getHttpStatus().value())));
    }

    @Override
    public String aliceBlueToken(String userId) {
        try {
            String userID = brokerCustomerDetailsRepo.findBrokerUserById(userId).getReferenceID();
            String sessionId = brokerSessionDetailsRepo.getSessionId(userId).getSessionId();
            return (userID + " " + sessionId);
        } catch (Exception ex) {
            return null;
        }
    }
}
