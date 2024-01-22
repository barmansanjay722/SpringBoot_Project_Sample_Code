package com.algotic.adapter.impl;

import com.algotic.adapter.StockMarketBroker;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.PaytmBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticOrderStatus;
import com.algotic.constants.AlgoticProductCode;
import com.algotic.constants.BrokerEnum;
import com.algotic.constants.Exchange;
import com.algotic.constants.PlaceOrder;
import com.algotic.constants.Retention;
import com.algotic.constants.TradeType;
import com.algotic.constants.TransactionType;
import com.algotic.constants.paytm.PaytmOrderStatus;
import com.algotic.constants.paytm.PaytmPriceType;
import com.algotic.constants.paytm.PaytmProductCode;
import com.algotic.constants.paytm.PaytmTransactionType;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.exception.PaytmErrorCode;
import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.AlgoticModifyRequest;
import com.algotic.model.request.AlgoticSquareOffRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.request.paytm.PaytmCancelOrderRequest;
import com.algotic.model.request.paytm.PlaceOrderRequest;
import com.algotic.model.response.AlgoticCancelOrderResponse;
import com.algotic.model.response.AlgoticSquareOffResponse;
import com.algotic.model.response.BookPositionResponse;
import com.algotic.model.response.HoldingAlgoticResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.OrderResponse;
import com.algotic.model.response.aliceblue.ModifyOrderResponse;
import com.algotic.model.response.paytm.OrderBookData;
import com.algotic.model.response.paytm.PaytmCancelOrderResponse;
import com.algotic.model.response.paytm.PaytmGeneralResponse;
import com.algotic.model.response.paytm.PaytmHoldingResult;
import com.algotic.model.response.paytm.PaytmLTPData;
import com.algotic.model.response.paytm.PaytmPositionData;
import com.algotic.model.response.paytm.PlaceOrderData;
import com.algotic.model.response.paytm.TradeBookData;
import com.algotic.services.UserService;
import com.algotic.services.impl.PaperOrderImpl;
import com.algotic.utils.AlgoticUtils;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 *
 * This adapter class is used to defined all methods of StockMarkerBroker and if
 * any others related to Paytm but having common request & Response object
 * Will do validation of request Object Will do conversation from common request
 * to Paytmspecifc request object Will do conversion from PaytmSpecific
 * Response to common Response object Will call Paytm api
 *
 */
@Slf4j
@Component
public class PaytmAdapter implements StockMarketBroker {

    @Autowired
    private PaytmBrokerApi brokerApi;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration config;

    @Autowired
    private PaperOrderImpl paperService;

    /**
     * Method used to convert into paytm specific order request/ response and
     * invoke the broker api class to place an order
     */
    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {
        log.info(config.getLogHandler()
                .getInfoLog(
                        "Place Order via Paytm broker", AlgoticUtils.objectToJsonString(orderRequest), userId, null));

        log.debug("Fetching Paytm token");
        String token = userService.getSessionToken(userId, BrokerEnum.PAYTM_MONEY);
        log.debug("Found token form Paytm");

        PlaceOrderRequest paytmOrderReq = generateOrderRequest(orderRequest);
        log.debug("Generated broker specific order rquest -> [" + AlgoticUtils.objectToJsonString(paytmOrderReq)
                + "] Placing order using paytm api.");

        PaytmGeneralResponse<PlaceOrderData> placeOrderRes = brokerApi.placeOrder(paytmOrderReq, token);

        return createAlgoticOrderResponse(placeOrderRes);
    }

    private PlaceOrderRequest generateOrderRequest(OrderRequest orderRequest) {
        PaytmPriceType orderType = PaytmPriceType.getPriceType(orderRequest.getPriceType());

        String productCode =
                PaytmProductCode.getProductCode(orderRequest.getProductCode()).name();

        String exchange = orderRequest.getExchange();

        if (exchange.equalsIgnoreCase(Exchange.BFO.name())) {
            exchange = Exchange.BSE.name();
        } else if (exchange.equalsIgnoreCase(Exchange.NFO.name())) {
            exchange = Exchange.NSE.name();
        }

        return PlaceOrderRequest.builder()
                .transactionType(PaytmTransactionType.getTransactionType(orderRequest.getTransactionType())
                        .name())
                .exchange(exchange)
                .segment("E")
                .productCode(productCode)
                .scripCode(orderRequest.getToken())
                .quantity(orderRequest.getQuantity().toString())
                .retension(Retention.DAY.name())
                .orderType(orderType.name())
                .price(
                        PaytmPriceType.MKT.equals(orderType)
                                ? BigDecimal.ZERO.toString()
                                : PaytmPriceType.SLM.equals(orderType)
                                        ? BigDecimal.ZERO.toString()
                                        : BigDecimal.valueOf(orderRequest.getPrice())
                                                .toString())
                .amoOrder("false")
                .source("W")
                .triggerPrice(
                        orderRequest.getTriggerPrice() == null
                                        || BigDecimal.valueOf(orderRequest.getTriggerPrice())
                                                        .compareTo(BigDecimal.ZERO)
                                                == 0
                                ? null
                                : BigDecimal.valueOf(orderRequest.getTriggerPrice())
                                        .toString())
                .build();
    }

    /**
     * This method is responsible for getting holding response data list After
     * receiving the list it convert to algotic holding specific response
     */
    @Override
    public List<HoldingAlgoticResponse> holdings(String userId) throws NoSuchAlgorithmException {
        try {
            log.info("Feching Paytm Money holding data.");
            String token = userService.getSessionToken(userId, BrokerEnum.PAYTM_MONEY);

            List<PaytmHoldingResult> paytmHoldingList = brokerApi.holding(token);
            List<HoldingAlgoticResponse> holdingAlgoticResponseList = new ArrayList<>();

            for (PaytmHoldingResult paytmHoldingData : paytmHoldingList) {
                String exchangeName = Exchange.NSE.name();
                String scriptCode = paytmHoldingData.getNseSecurityId();

                if (!paytmHoldingData.getBseSecurityId().equals("0")
                        && !paytmHoldingData.getNseSecurityId().equals("0")) {
                    exchangeName = Exchange.NSE.name();
                    scriptCode = paytmHoldingData.getNseSecurityId();
                } else if (paytmHoldingData.getNseSecurityId().equals("0")) {
                    exchangeName = Exchange.BSE.name();
                    scriptCode = paytmHoldingData.getBseSecurityId();
                }

                Map<String, String> pdcAndLtpData = paperService.getPdcAndLtp(scriptCode, exchangeName);
                holdingAlgoticResponseList.add(
                        createGlobleHolding(paytmHoldingData, pdcAndLtpData, scriptCode, exchangeName));
            }
            return holdingAlgoticResponseList;

        } catch (AlgoticException e) {
            throw e;
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private HoldingAlgoticResponse createGlobleHolding(
            PaytmHoldingResult paytmHoldingData,
            Map<String, String> pdcAndLtpData,
            String scriptCode,
            String exchangeName) {
        HoldingAlgoticResponse algoticHoldingResponse = new HoldingAlgoticResponse();

        algoticHoldingResponse.setExchange(exchangeName);
        algoticHoldingResponse.setInstrumentName(paytmHoldingData.getDisplayName());
        algoticHoldingResponse.setIsStock(true);
        algoticHoldingResponse.setLtp(paytmHoldingData.getLastTradedPrice());
        algoticHoldingResponse.setOrderType(AlgoticProductCode.CNC.name());
        algoticHoldingResponse.setPrice(paytmHoldingData.getCostPrice());
        algoticHoldingResponse.setQuantity(paytmHoldingData.getQuantity());
        algoticHoldingResponse.setToken(scriptCode);
        algoticHoldingResponse.setTradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()));
        algoticHoldingResponse.setTradingSymbol(paytmHoldingData.getDisplayName());
        if (pdcAndLtpData != null) {
            algoticHoldingResponse.setPdc(pdcAndLtpData.get("pdc"));
        }

        return algoticHoldingResponse;
    }

    /**
     *This method is responsible for getting Paytm money order position
     *After getting order position, it convert paytm money position response to globle position response
     */
    @Override
    public List<BookPositionResponse> bookPosition(String userId) throws NoSuchAlgorithmException {
        try {
            log.info("Start Fetching order position for broker Paytm Money.");
            String token = userService.getSessionToken(userId, BrokerEnum.PAYTM_MONEY);
            List<PaytmPositionData> paytmPositionList = brokerApi.position(token);
            List<BookPositionResponse> bookPositionResponseList = new ArrayList<>();

            for (PaytmPositionData paytmPosition : paytmPositionList) {

                String exchange = paytmPosition.getExchange();
                String scripId = paytmPosition.getSecurityId();
                String scripType = "EQUITY";

                log.info("Fetching Last Traded Price for position data.");
                List<PaytmLTPData> ltpData = brokerApi.getLTPData(exchange, scripId, scripType, token);

                bookPositionResponseList.add(
                        createGloblePosition(paytmPosition, ltpData.get(0).getLastTradedPrice()));
            }
            return bookPositionResponseList;
        } catch (AlgoticException e) {
            throw e;
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private BookPositionResponse createGloblePosition(PaytmPositionData paytmPosition, String lastTradedPrice) {
        BookPositionResponse globlePositionResponse = new BookPositionResponse();
        globlePositionResponse.setBlQty(paytmPosition.getTotalBuyQuantity());
        globlePositionResponse.setBuyAverage(paytmPosition.getTotalBuyAverage());
        globlePositionResponse.setExchange(paytmPosition.getExchange());
        globlePositionResponse.setInstrumentName(paytmPosition.getDisplayName());
        globlePositionResponse.setIsStock(paytmPosition.getExchange().equalsIgnoreCase(Exchange.NSE.name())
                || paytmPosition.getExchange().equalsIgnoreCase(Exchange.BSE.name()));
        globlePositionResponse.setLastTradePrice(lastTradedPrice);
        globlePositionResponse.setNetBuyAvgPrice(paytmPosition.getTotalBuyAverage());
        globlePositionResponse.setNetBuyQty(paytmPosition.getTotalBuyQuantity());
        globlePositionResponse.setNetQuantity(paytmPosition.getNetQuantity());
        globlePositionResponse.setNetSellAvgPrice(paytmPosition.getTotalSellAverage());
        globlePositionResponse.setNetSellQty(paytmPosition.getTotalSellQuantity());

        globlePositionResponse.setOrderType(EnumUtils.getEnum(
                        PaytmProductCode.class, paytmPosition.getProduct().toUpperCase())
                .getAlgoticProductCode());
        globlePositionResponse.setProfitAndLoss(paytmPosition.getProfitAndLoss());
        globlePositionResponse.setQuantity(paytmPosition.getNetQuantity());
        globlePositionResponse.setSellAverage(paytmPosition.getTotalSellAverage());
        globlePositionResponse.setToken(paytmPosition.getSecurityId());
        globlePositionResponse.setTradeType(TradeType.LIVE.name());
        globlePositionResponse.setTradingSymbol(paytmPosition.getDisplayName());
        return globlePositionResponse;
    }

    /**
     *This method is responsible for canceling the pending orders.
     *Firstly fetching the orderBook data than filter that list and filter it as per algoticRequest
     *And call the Cancel Order API with filter data and cancel it
     */
    @Override
    public AlgoticCancelOrderResponse cancelOrder(AlgoticCancelOrderRequest algoticRequest, String userId) {

        log.info("Feching paytm session token where userId -> [{}]", userId);
        String token = userService.getSessionToken(userId, BrokerEnum.PAYTM_MONEY);

        log.info("Getting orderBook for cancel order, where order no -> [{}]", algoticRequest.getNestOrderNumber());
        PaytmGeneralResponse<OrderBookData> orderBookResponse = brokerApi.getOrderBook(token);

        log.info("Filtering orderBook data related to orderId -> [{}]", algoticRequest.getNestOrderNumber());
        OrderBookData orderBookData = orderBookResponse.getData().stream()
                .filter(o -> o.orderNumber().equals(algoticRequest.getNestOrderNumber()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No Order data is matches with order no -> [{}]", algoticRequest.getNestOrderNumber());
                    return new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
                });

        PaytmCancelOrderResponse cancelOrderResponse = brokerApi.cancelOrder(createCancelRequest(orderBookData), token);

        AlgoticCancelOrderResponse algoticResponse = new AlgoticCancelOrderResponse();

        if (cancelOrderResponse.getStatus().equalsIgnoreCase("error")) {
            log.error("Some error occurred while canceling order using paytm api [" + cancelOrderResponse.getMessage()
                    + "]");
            algoticResponse.setStatus(cancelOrderResponse.getStatus());
            algoticResponse.setErrorode(PaytmErrorCode.getErrorCodeByRefId(cancelOrderResponse.getErrorCode()));
            algoticResponse.setException(true);

        } else {
            log.info("Order canceled successfully. Cancel order status -> [" + cancelOrderResponse.getStatus()
                    + "] and message -> [{" + cancelOrderResponse.getMessage() + "}]");
            algoticResponse.setStatus(cancelOrderResponse.getStatus());
        }

        return algoticResponse;
    }

    private PaytmCancelOrderRequest createCancelRequest(OrderBookData orderBookData) {
        log.info("Converting paytm order book response to paytm cancel order request.");

        PaytmCancelOrderRequest paytmCancelOrderRequest = new PaytmCancelOrderRequest();

        paytmCancelOrderRequest.setExchange(orderBookData.exchange());
        paytmCancelOrderRequest.setGroupId(Integer.parseInt(orderBookData.groupId()));
        paytmCancelOrderRequest.setMarketType(orderBookData.marketType());
        paytmCancelOrderRequest.setOffMarketFlag(orderBookData.amoOrder());
        paytmCancelOrderRequest.setOrderNumber(orderBookData.orderNumber());
        paytmCancelOrderRequest.setOrderType(orderBookData.orderType());
        paytmCancelOrderRequest.setPrice(orderBookData.price());
        paytmCancelOrderRequest.setTriggerPrice(orderBookData.triggerPrice());
        paytmCancelOrderRequest.setProduct(orderBookData.productCode());
        paytmCancelOrderRequest.setQuantity(orderBookData.quantity());
        paytmCancelOrderRequest.setSecurityId(orderBookData.scripCode());
        paytmCancelOrderRequest.setSegment(orderBookData.segment());
        paytmCancelOrderRequest.setSerialNumber(Integer.parseInt(orderBookData.serialNumber()));
        paytmCancelOrderRequest.setSource("W");
        paytmCancelOrderRequest.setTrnxType(orderBookData.transactionType());
        paytmCancelOrderRequest.setValidity(orderBookData.retension());

        return paytmCancelOrderRequest;
    }

    /**
     *This method is responsible for fetching order book data.
     */
    @Override
    public List<OrderAndTradeBookResponse> getOrderBook(String userId) {
        log.info("Method start -> getOrderBook");
        log.info("Fetching Paytm token");
        String token = userService.getSessionToken(userId, BrokerEnum.PAYTM_MONEY);

        log.debug("Found jwt auth token from paytm & trying to fetch order book...");
        PaytmGeneralResponse<OrderBookData> orderBook = brokerApi.getOrderBook(token);
        List<OrderAndTradeBookResponse> orderAndTradeBookResponses = new ArrayList<>();
        if (StringUtils.equals("error", orderBook.getStatus())) {
            log.error("Some error occurred while getting order book [{}]", orderBook.getMessage());
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
        if (!CollectionUtils.isEmpty(orderBook.getData())) {
            log.info(
                    "Found order book from paytm of size {}",
                    orderBook.getData().size());
            orderBook.getData().stream().forEach(e -> {
                OrderAndTradeBookResponse orderAndTradeBookResponse = createOrderAndTradeBookResponse(e);
                orderAndTradeBookResponses.add(orderAndTradeBookResponse);
            });
        }

        return orderAndTradeBookResponses;
    }

    private <T> OrderAndTradeBookResponse createOrderAndTradeBookResponse(T bookData) {
        String time = null;
        TransactionType type = null;
        String tradingSymbol = null;
        String price = null;
        Integer quantity = null;
        String status = null;
        String orderNumber = null;
        String exchange = null;
        String product = null;

        if (bookData instanceof OrderBookData orderBookData) {
            time = orderBookData.orderDateTime();
            type = PaytmTransactionType.B.name().equals(orderBookData.transactionType())
                    ? TransactionType.BUY
                    : TransactionType.SELL;
            tradingSymbol = orderBookData.displayName();
            price = AlgoticUtils.getPriceData(
                    orderBookData.price(), orderBookData.triggerPrice(), orderBookData.avgTradePrice());
            product = getProductData(orderBookData.orderType(), orderBookData.productCode());
            quantity = orderBookData.quantity();
            status = orderBookData.status();
            orderNumber = orderBookData.orderNumber();
            exchange = orderBookData.exchangeOrderTime();
        }
        return OrderAndTradeBookResponse.builder()
                .time(StringUtils.split(time, " ")[1])
                .tradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()))
                .type(type.name())
                .tradingSymbol(tradingSymbol)
                .instrument(tradingSymbol)
                .price(price)
                .product(product)
                .quantity(quantity)
                .status(AlgoticUtils.convertToPascalCase(
                        PaytmOrderStatus.getOrderStatus(status).getAlgoticOrderStatus()))
                .nestOrderNumber(orderNumber)
                .exchange(exchange)
                .build();
    }

    private String getProductData(String orderType, String productType) {
        String priceType =
                EnumUtils.getEnum(PaytmPriceType.class, orderType.toUpperCase()).getAlgoticPriceType();

        String productCode = EnumUtils.getEnum(PaytmProductCode.class, productType.toUpperCase())
                .getAlgoticProductCode();

        if (StringUtils.isAnyEmpty(priceType, productCode)) {
            return "";
        }

        return StringUtils.join(productCode, " / ", priceType);
    }

    /**
     *This method is reponsible for getting tradeBook data.
     */
    @Override
    public List<OrderAndTradeBookResponse> getTradeBook() {
        log.info("Method start -> getTradeBook");
        log.info("Fetching Paytm token");
        String token = userService.getSessionToken(jwtHelper.getUserId(), BrokerEnum.PAYTM_MONEY);
        log.debug("Found auth token from paytm & trying to fetch order book...");

        List<OrderAndTradeBookResponse> orderAndTradeBookResponses = new ArrayList<>();

        // Retrieving the current request attributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        getTradedStatusOrder(token).parallelStream().forEach(orderBookData -> {

            // Set the request context before processing each item
            RequestContextHolder.setRequestAttributes(requestAttributes);

            String orderNumber = orderBookData.orderNumber();
            String legNo = orderBookData.legNumber();
            String segment = orderBookData.segment();

            log.debug(config.getLogHandler()
                    .getInfoLog(
                            "Fetching trade details of orderNumber" + orderNumber,
                            String.format("orderNo. : [%s], legNo : [%s], segment : [%s]", orderNumber, legNo, segment),
                            jwtHelper.getUserId(),
                            null));

            PaytmGeneralResponse<TradeBookData> tradeBookData =
                    brokerApi.getTradeBook(orderNumber, legNo, segment, token);

            if (!CollectionUtils.isEmpty(tradeBookData.getData())) {
                log.debug(
                        "Found trade book from paytm of size {}",
                        tradeBookData.getData().size());
                OrderAndTradeBookResponse orderAndTradeBookResponse = createTradeBookResponse(
                        orderBookData, tradeBookData.getData().get(0));
                orderAndTradeBookResponses.add(orderAndTradeBookResponse);
            }
        });
        log.info("Total no. of trade book details are {}", orderAndTradeBookResponses.size());
        return orderAndTradeBookResponses;
    }

    /**
     * This method help in transformation of paytm specific tradebook response into
     * algotic specific response for displaying purpose
     *
     * @param orderBookData
     * @param tradeBookData
     * @return
     */
    private OrderAndTradeBookResponse createTradeBookResponse(
            OrderBookData orderBookData, TradeBookData tradeBookData) {

        if (Objects.isNull(tradeBookData) || Objects.isNull(orderBookData)) {
            log.warn("Either tradeBookData or orderBookData is null");
            return new OrderAndTradeBookResponse();
        }

        TransactionType type = PaytmTransactionType.B.name().equals(orderBookData.transactionType())
                ? TransactionType.BUY
                : TransactionType.SELL;
        return OrderAndTradeBookResponse.builder()
                .time(StringUtils.split(tradeBookData.tradeTime(), " ")[1])
                .tradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()))
                .type(AlgoticUtils.convertToPascalCase(type.name()))
                .tradingSymbol(orderBookData.displayName())
                .instrument(orderBookData.displayName())
                .price(tradeBookData.tradePrice())
                .product(getProductData(orderBookData.orderType(), orderBookData.productCode()))
                .quantity(tradeBookData.quantity())
                .status(AlgoticUtils.convertToPascalCase(AlgoticOrderStatus.COMPLETE.name()))
                .nestOrderNumber(orderBookData.orderNumber())
                .exchange(orderBookData.exchange())
                .build();
    }

    /**
     * This method responsible to retrieve all traded status order details requested
     * in a day by using order book api
     *
     * @param token
     * @return List<OrderBookData>
     * @see List<#OrderBookData>
     */
    private List<OrderBookData> getTradedStatusOrder(String token) {
        log.info(config.getLogHandler()
                .getInfoLog(
                        "Method start -> getTradedStatusOrder",
                        "fetching [traded] status order details from order book",
                        jwtHelper.getUserId(),
                        null));
        PaytmGeneralResponse<OrderBookData> orderBookRes = brokerApi.getOrderBook(token);
        if (CollectionUtils.isEmpty(orderBookRes.getData())) {
            log.debug("order details not found {}", orderBookRes.getMessage());
            return Collections.emptyList();
        }
        List<OrderBookData> tradedOrderBooks = orderBookRes.getData().stream()
                .filter(e -> PaytmOrderStatus.TRADED.getPaytmOrderStatus().equals(e.status()))
                .toList();
        log.debug("Found {} of traded status orders", tradedOrderBooks.size());
        return tradedOrderBooks;
    }

    @Override
    public ModifyOrderResponse modifyOrder(AlgoticModifyRequest modifyRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *This method is responsible for doing individual open position's squareOff
     */
    @Override
    public AlgoticSquareOffResponse squareOff(AlgoticSquareOffRequest algoticSquareOffRequest) {
        String token = userService.getSessionToken(jwtHelper.getUserId(), BrokerEnum.PAYTM_MONEY);
        log.info("fetching all position for squreOff");
        List<PaytmPositionData> positionlList = brokerApi.position(token);

        log.info("Filter position list related to token -> {}", algoticSquareOffRequest.getToken());
        PaytmPositionData positionData = positionlList.stream()
                .filter(p -> p.getSecurityId()
                                .equals(algoticSquareOffRequest.getToken().toString())
                        && algoticSquareOffRequest
                                .getProductCode()
                                .equalsIgnoreCase(EnumUtils.getEnum(
                                                PaytmProductCode.class,
                                                p.getProduct().toUpperCase())
                                        .getAlgoticProductCode()))
                .findFirst()
                .orElseThrow(() -> new AlgoticException(CommonErrorCode.DATA_NOT_FOUND));

        PlaceOrderRequest placeOrderRequest = convertToOrderRequest(positionData);

        log.info("Placing order for square off position.");
        PaytmGeneralResponse<PlaceOrderData> placeOrderRes = brokerApi.placeOrder(placeOrderRequest, token);

        OrderResponse algoticOrderRes = createAlgoticOrderResponse(placeOrderRes);

        return new AlgoticSquareOffResponse(algoticOrderRes.getOrderStatus(), algoticOrderRes.getErrorMessage());
    }

    private PlaceOrderRequest convertToOrderRequest(PaytmPositionData positionData) {
        log.info("Converting paytm position data into placeOrderRequest for squareOff.");
        String buyOrSell = PaytmTransactionType.S.name();
        Integer quantity = Integer.parseInt(positionData.getTotalBuyQuantity())
                - Integer.parseInt(positionData.getTotalSellQuantity());

        if (quantity < 0) {
            buyOrSell = PaytmTransactionType.B.name();
            quantity = quantity * -1;
        }
        if (quantity == 0) {
            return null;
        }
        return PlaceOrderRequest.builder()
                .transactionType(buyOrSell)
                .exchange(positionData.getExchange())
                .segment("E")
                .productCode(positionData.getProduct())
                .scripCode(positionData.getSecurityId())
                .quantity(quantity.toString())
                .retension(Retention.DAY.name())
                .price(BigDecimal.ZERO.toString())
                .orderType(PaytmPriceType.MKT.name())
                .amoOrder("false")
                .source("W")
                .build();
    }

    /**
     *This method is responsible for squareOff all open positions.
     */
    @Override
    public AlgoticSquareOffResponse squareOffAll() {
        log.info("Adapter calling for squareOffAll()");
        String token = userService.getSessionToken(jwtHelper.getUserId(), BrokerEnum.PAYTM_MONEY);

        log.info("Fetching all position for squareOffAll.");
        List<PaytmPositionData> positionlList = brokerApi.position(token);

        log.info("Placing orders for squareOff of position data.");
        positionlList.stream().forEach(paytmPositionData -> {
            PlaceOrderRequest placeOrderRequest = convertToOrderRequest(paytmPositionData);
            if (placeOrderRequest != null) {
                brokerApi.placeOrder(placeOrderRequest, token);
            }
        });

        return new AlgoticSquareOffResponse("Completed", "SquareOff All Completed");
    }

    @Override
    public BrokerEnum getBrokerName() {
        return BrokerEnum.PAYTM_MONEY;
    }

    private OrderResponse createAlgoticOrderResponse(PaytmGeneralResponse<PlaceOrderData> placeOrderRes) {
        log.info("Converting paytm place order response to algotic order response");
        OrderResponse algoticOrderRes = new OrderResponse();
        if (!CollectionUtils.isEmpty(placeOrderRes.getData())) {
            algoticOrderRes.setOrderRefNumber(placeOrderRes.getData().get(0).uniqueOrderId());
        }

        if (StringUtils.equalsIgnoreCase("error", placeOrderRes.getStatus())) {
            log.error("Some error occurred while placing order using paytm api [" + placeOrderRes.getMessage() + "]");
            algoticOrderRes.setOrderStatus(PlaceOrder.FAILED.name());
            algoticOrderRes.setErrorMessage(placeOrderRes.getMessage());
            algoticOrderRes.setException(true);
            algoticOrderRes.setErrorCode(PaytmErrorCode.getErrorCodeByRefId(
                    placeOrderRes.getData().get(0).errorCode()));
        } else {
            algoticOrderRes.setOrderStatus(PlaceOrder.PLACED.name());
        }

        log.info(config.getLogHandler()
                .getInfoLog(
                        "Succesfully converted into algotic response",
                        AlgoticUtils.objectToJsonString(algoticOrderRes),
                        null,
                        null));
        return algoticOrderRes;
    }
}
