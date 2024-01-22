package com.algotic.adapter.impl;

import com.algotic.adapter.StockMarketBroker;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.MotilalBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.*;
import com.algotic.constants.motilal.MotilalExchangeEnum;
import com.algotic.constants.motilal.MotilalOrderStatus;
import com.algotic.constants.motilal.MotilalPriceType;
import com.algotic.constants.motilal.MotilalProductCode;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.exception.MotilalBrokerErrorCode;
import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.AlgoticModifyRequest;
import com.algotic.model.request.AlgoticSquareOffRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.request.motilal.CancelOrderRequest;
import com.algotic.model.request.motilal.PlaceOrderRequest;
import com.algotic.model.response.AlgoticCancelOrderResponse;
import com.algotic.model.response.AlgoticSquareOffResponse;
import com.algotic.model.response.BookPositionResponse;
import com.algotic.model.response.HoldingAlgoticResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.OrderResponse;
import com.algotic.model.response.aliceblue.ModifyOrderResponse;
import com.algotic.model.response.motilal.CancelOrderResponse;
import com.algotic.model.response.motilal.MotilalHoldingData;
import com.algotic.model.response.motilal.MotilalPositionData;
import com.algotic.model.response.motilal.OrderBookData;
import com.algotic.model.response.motilal.OrderBookResponse;
import com.algotic.model.response.motilal.PlaceOrderResponse;
import com.algotic.model.response.motilal.TradeBookData;
import com.algotic.model.response.motilal.TradeBookResponse;
import com.algotic.services.UserService;
import com.algotic.services.impl.PaperOrderImpl;
import com.algotic.utils.AlgoticUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 *
 * This adapter class is used to defined all methods of StockMarkerBroker and if
 * any others related to Motilal but having common request & Response object
 * Will do validation of request Object Will do conversation from common request
 * to Motilalspecifc request object Will do conversion from MotilalSpecific
 * Response to common Response object Will call MotilalApi
 *
 */
@Slf4j
@Component
public class MotilalAdapter implements StockMarketBroker {

    @Autowired
    private MotilalBrokerApi brokerApi;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration config;

    @Autowired
    private PaperOrderImpl paperService;

    /**
     * Method used to convert into motilal specific order request/ response and
     * invoke the broker api class to place an order
     */
    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {
        log.info(config.getLogHandler()
                .getInfoLog("Place Order via Motilal broker", orderRequest.toString(), userId, null));

        validateOrderRequest(orderRequest);

        log.info("Fetching Motilal token");
        String token = userService.getMotilalToken(userId);
        log.info("Found token form Motilal");

        PlaceOrderRequest orderReq = generateOrderRequest(orderRequest);
        log.info("Generated order rquest -> [" + AlgoticUtils.objectToJsonString(orderReq)
                + "] Placing order using motilal api.");
        PlaceOrderResponse orderRes = brokerApi.placeOrder(orderReq, token);
        OrderResponse algoticOrderRes = new OrderResponse();
        algoticOrderRes.setOrderRefNumber(orderRes.uniqueorderid());

        if (StringUtils.equals("ERROR", orderRes.status())) {
            log.error("Some error occurred while placing order using motilal api [" + orderRes.message() + "]");
            algoticOrderRes.setOrderStatus(PlaceOrder.FAILED.name());
            algoticOrderRes.setErrorMessage(orderRes.message());
            algoticOrderRes.setException(true);
            algoticOrderRes.setErrorCode(MotilalBrokerErrorCode.getErrorCodeByRefId(orderRes.errorcode()));
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

    /**
     *return holdingAlgoticResponse
     *get motilalHoldingResponse of motilal
     *convert motilalHoldingResponse to holdingAlgoticResponse
     */
    @Override
    public List<HoldingAlgoticResponse> holdings(String userId) throws NoSuchAlgorithmException {
        try {
            log.info("Feching Motilal Oswal holding data.");
            String token = userService.getMotilalToken(userId);

            List<MotilalHoldingData> motilalHoldingList = brokerApi.holding(token);
            List<HoldingAlgoticResponse> holdingAlgoticResponseList = new ArrayList<>();

            for (MotilalHoldingData motilalHoldingData : motilalHoldingList) {

                if (motilalHoldingData.getTotalQuantity().equalsIgnoreCase("0")
                        || motilalHoldingData.getTotalQuantity() == null) {
                    log.info(
                            "Holding data has zero quantity, TotalQuantity -> {}",
                            motilalHoldingData.getTotalQuantity());
                    continue;
                }

                String exchangeName = Exchange.NSE.name();
                String scriptCode = motilalHoldingData.getNseSymbolToken();

                if (!motilalHoldingData.getBseScripCode().equals("0")
                        && !motilalHoldingData.getNseSymbolToken().equals("0")) {
                    exchangeName = Exchange.NSE.name();
                    scriptCode = motilalHoldingData.getNseSymbolToken();
                } else if (motilalHoldingData.getNseSymbolToken() == null
                        || motilalHoldingData.getNseSymbolToken().equals("0")) {
                    exchangeName = Exchange.BSE.name();
                    scriptCode = motilalHoldingData.getBseScripCode();
                }

                log.info("Trying to fetching ltp data for holding {}", motilalHoldingData.getScripName());
                Map<String, String> pdcAndLtpData = paperService.getPdcAndLtp(scriptCode, exchangeName);

                holdingAlgoticResponseList.add(
                        createGlobleHolding(motilalHoldingData, pdcAndLtpData, exchangeName, scriptCode));
            }
            return holdingAlgoticResponseList;

        } catch (AlgoticException e) {
            throw e;
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private HoldingAlgoticResponse createGlobleHolding(
            MotilalHoldingData motilalHoldinData,
            Map<String, String> pdcAndLtpData,
            String exchangeName,
            String scriptCode) {
        log.info("Converting motilal oswal holding response to algotic holding response.");
        String ltp = null;
        String pdc = null;

        if (pdcAndLtpData != null) {
            ltp = pdcAndLtpData.get("ltp");
            pdc = pdcAndLtpData.get("pdc");
        }

        return HoldingAlgoticResponse.builder()
                .tradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()))
                .quantity(motilalHoldinData.getTotalQuantity())
                .instrumentName(motilalHoldinData.getScripName())
                .tradingSymbol(motilalHoldinData.getScripName())
                .orderType(AlgoticProductCode.CNC.name())
                .ltp(ltp)
                .pdc(pdc)
                .isStock(true)
                .price(motilalHoldinData.getBuyAvgPrice())
                .token(scriptCode)
                .exchange(exchangeName)
                .build();
    }

    /**
     *return BookPositionResponse
     *get MotilalPositionData of motilal
     *convert MotilalPositionData to BookPositionResponse
     */
    @Override
    public List<BookPositionResponse> bookPosition(String userId) throws NoSuchAlgorithmException {
        try {
            log.info("Fetching order position for broker Motilal Oswal.");
            String token = userService.getMotilalToken(userId);
            List<MotilalPositionData> motilalPositionList = brokerApi.position(token);
            List<BookPositionResponse> bookPositionResponseList = new ArrayList<>();

            for (MotilalPositionData motilalPosition : motilalPositionList) {
                BookPositionResponse bookPositionResponse = createGloblePosition(motilalPosition);
                bookPositionResponseList.add(bookPositionResponse);
            }
            return bookPositionResponseList;
        } catch (AlgoticException e) {
            throw e;
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private BookPositionResponse createGloblePosition(MotilalPositionData motilalPosition) {
        log.info("Converting Motilal Oswal position response to algotic holding response.");
        BookPositionResponse bookPositionResponse = new BookPositionResponse();
        BigDecimal buyAvgPrice = BigDecimal.ZERO;
        BigDecimal sellAvgPrice = BigDecimal.ZERO;

        // calculating average buy price
        if (Integer.parseInt(motilalPosition.getBuyQuantity()) != 0) {
            buyAvgPrice = new BigDecimal(motilalPosition.getBuyAmount())
                    .divide(new BigDecimal(motilalPosition.getBuyQuantity()), RoundingMode.HALF_EVEN);
        }

        // calculating net sell average price
        if (Integer.parseInt(motilalPosition.getSellQuantity()) != 0) {
            sellAvgPrice = new BigDecimal(motilalPosition.getSellAmount())
                    .divide(new BigDecimal(motilalPosition.getSellQuantity()), RoundingMode.HALF_UP);
        }

        bookPositionResponse.setNetSellAvgPrice(sellAvgPrice.toString());
        bookPositionResponse.setBlQty(motilalPosition.getBuyQuantity());
        bookPositionResponse.setBuyAverage(motilalPosition.getBuyAmount());
        bookPositionResponse.setExchange(motilalPosition.getExchange());
        bookPositionResponse.setInstrumentName(motilalPosition.getSymbol());

        bookPositionResponse.setIsStock(motilalPosition.getExchange().equalsIgnoreCase(Exchange.NSE.name())
                || motilalPosition.getExchange().equalsIgnoreCase(Exchange.BSE.name()));

        bookPositionResponse.setLastTradePrice(motilalPosition.getLtp());
        bookPositionResponse.setNetBuyAvgPrice(buyAvgPrice.toString());
        bookPositionResponse.setNetBuyQty(motilalPosition.getDayBuyQuantity());
        bookPositionResponse.setNetQuantity(new BigDecimal(motilalPosition.getBuyQuantity())
                .subtract(new BigDecimal(motilalPosition.getSellQuantity()))
                .toString());

        bookPositionResponse.setNetSellQty(motilalPosition.getDaySellQuantity());

        bookPositionResponse.setOrderType(EnumUtils.getEnum(MotilalProductCode.class, motilalPosition.getProductName())
                .getValue());

        bookPositionResponse.setProfitAndLoss(motilalPosition.getActualBookedProfitLoss());

        bookPositionResponse.setQuantity(new BigDecimal(motilalPosition.getBuyQuantity())
                .subtract(new BigDecimal(motilalPosition.getSellQuantity()))
                .toString());

        bookPositionResponse.setSellAverage(motilalPosition.getCfSellAmount());
        bookPositionResponse.setToken(motilalPosition.getSymbolToken());
        bookPositionResponse.setTradeType(TradeType.LIVE.name());
        bookPositionResponse.setTradingSymbol(motilalPosition.getSymbol());

        return bookPositionResponse;
    }

    /**
     * Method used to convert into motilal specific cancel request/ response and also
     * invoke the broker api class to cancel an open order
     */
    @Override
    public AlgoticCancelOrderResponse cancelOrder(AlgoticCancelOrderRequest algoticRequest, String userId) {

        log.info("Fetching Motilal token");
        String token = userService.getMotilalToken(userId);
        log.info("Found token form Motilal");

        CancelOrderRequest cancelOrderReq = new CancelOrderRequest(algoticRequest.getNestOrderNumber());
        log.info("Generated cancel order rquest -> [" + AlgoticUtils.objectToJsonString(algoticRequest)
                + "] Canceling open order using motilal broker api.");
        CancelOrderResponse canOrderRes = brokerApi.cancelOrder(cancelOrderReq, token);
        AlgoticCancelOrderResponse algoticOrderRes = new AlgoticCancelOrderResponse();
        algoticOrderRes.setNestOrderNumber(algoticRequest.getNestOrderNumber());
        algoticOrderRes.setStatus(canOrderRes.getStatus());

        if (StringUtils.equals("ERROR", canOrderRes.getStatus())) {
            log.error("Some error occurred while Canceling open order using motilal api [" + canOrderRes.getMessage()
                    + "]");
            algoticOrderRes.setException(true);
            algoticOrderRes.setErrorode(MotilalBrokerErrorCode.getErrorCodeByRefId(canOrderRes.getErrorcode()));
        }

        log.info(config.getLogHandler()
                .getInfoLog(
                        "Succesfully converted into algotic response",
                        AlgoticUtils.objectToJsonString(algoticOrderRes),
                        null,
                        null));
        return algoticOrderRes;
    }

    @Override
    public ModifyOrderResponse modifyOrder(AlgoticModifyRequest modifyRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BrokerEnum getBrokerName() {
        return BrokerEnum.MOTILAL_OSWAL;
    }

    /**
     * Method used to create an order request for placing an order
     * @param orderRequest
     * @return
     */
    private PlaceOrderRequest generateOrderRequest(OrderRequest orderRequest) {
        log.info("Method start -> generateOrderRequest");
        return PlaceOrderRequest.builder()
                .buyorsell(orderRequest.getTransactionType())
                .triggerprice(orderRequest.getTriggerPrice())
                .orderduration(Retention.DAY.name())
                .quantityinlot(orderRequest.getQuantity())
                .price(orderRequest.getPrice())
                .ordertype(MotilalPriceType.getPriceType(orderRequest.getPriceType())
                        .name())
                .producttype(MotilalProductCode.getProductType(orderRequest.getProductCode())
                        .name())
                .exchange(MotilalExchangeEnum.getExchangeEnum(orderRequest.getExchange())
                        .name())
                .symboltoken(Integer.valueOf(orderRequest.getToken()))
                .disclosedquantity(0)
                .amoorder("N")
                .build();
    }

    /**
     * Method used to invoke the broker api to get an user order book & convert it's
     * response into Algotic order book response
     *
     * @return
     */
    @Override
    public List<OrderAndTradeBookResponse> getOrderBook(String userId) {
        log.info("Method start -> getOrderBook");
        log.info("Fetching Motilal token");
        String token = userService.getMotilalToken(userId);
        log.debug("Found auth token from motilal & trying to fetch order book...");
        OrderBookResponse orderBook = brokerApi.getOrderBook(token);
        List<OrderAndTradeBookResponse> orderAndTradeBookResponses = new ArrayList<>();
        if (StringUtils.equals("ERROR", orderBook.getStatus())) {
            log.error("Some error occurred while getting order book [{}]", orderBook.getMessage());
            throw new AlgoticException(MotilalBrokerErrorCode.getErrorCodeByRefId(orderBook.getErrorCode()));
        }
        if (!CollectionUtils.isEmpty(orderBook.getData())) {
            log.info(
                    "Found order book from motilal of size {}",
                    orderBook.getData().size());
            orderBook.getData().stream().forEach(e -> {
                OrderAndTradeBookResponse orderAndTradeBookResponse = createOrderAndTradeBookResponse(e);
                orderAndTradeBookResponses.add(orderAndTradeBookResponse);
            });
        }

        return orderAndTradeBookResponses;
    }

    /**
     * This method convert motilalorderbook & tradebook into algotic specifc order/trade book response
     * @param <T> Either TradeBookData or OrderBookData
     * @return OrderAndTradeBookResponse
     */
    private <T> OrderAndTradeBookResponse createOrderAndTradeBookResponse(T bookData) {
        String time = null;
        String type = null;
        String tradingSymbol = null;
        String price = null;
        Integer quantity = null;
        String status = null;
        String orderNumber = null;
        String exchange = null;
        String product = null;

        if (bookData instanceof OrderBookData orderBookData) {
            time = orderBookData.getRecordinserttime();
            type = orderBookData.getBuyorsell();
            tradingSymbol = orderBookData.getSymbol();
            price = getPriceData(orderBookData);
            product = getProductData(orderBookData.getOrdertype(), orderBookData.getProducttype());
            quantity = orderBookData.getOrderqty();
            status = orderBookData.getOrderstatus();
            orderNumber = orderBookData.getUniqueorderid();
            exchange = orderBookData.getExchange();

        } else if (bookData instanceof TradeBookData tradeBookData) {
            time = tradeBookData.getTradetime();
            type = tradeBookData.getBuyorsell();
            tradingSymbol = tradeBookData.getSymbol();
            price = tradeBookData.getTradeprice().toString();
            quantity = tradeBookData.getTradeqty();
            status = MotilalOrderStatus.TRADED.name();
            orderNumber = tradeBookData.getUniqueorderid();
            exchange = tradeBookData.getExchange();
            product = EnumUtils.getEnum(MotilalProductCode.class, tradeBookData.getProducttype())
                    .getValue();
        }

        return OrderAndTradeBookResponse.builder()
                .time(StringUtils.split(time, " ")[1])
                .tradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()))
                .type(type)
                .tradingSymbol(tradingSymbol)
                .instrument(tradingSymbol)
                .price(price)
                .product(product)
                .quantity(quantity)
                .status(AlgoticUtils.convertToPascalCase(
                        EnumUtils.getEnum(MotilalOrderStatus.class, status.toUpperCase())
                                .getAlgoticOrderStatus()))
                .nestOrderNumber(orderNumber)
                .exchange(exchange)
                .build();
    }

    private String getProductData(String orderType, String productType) {
        MotilalPriceType priceType = MotilalPriceType.getMotilalPriceType(orderType.toUpperCase());
        String algoticProductType =
                EnumUtils.getEnum(MotilalProductCode.class, productType).getValue();

        if (StringUtils.isAnyEmpty(priceType.name(), algoticProductType)) {
            return "";
        }

        return StringUtils.join(algoticProductType, " / ", priceType);
    }

    private String getPriceData(OrderBookData orderBookData) {
        BigDecimal priceOpt = orderBookData.getPrice();
        BigDecimal triggerOpt = orderBookData.getTriggerPrice();
        BigDecimal averagePriceOpt = orderBookData.getAverageprice();
        String priceData;

        if (triggerOpt != null && triggerOpt.compareTo(BigDecimal.ZERO) > 0) {

            if (priceOpt != null && priceOpt.compareTo(BigDecimal.ZERO) > 0) {
                priceData = StringUtils.join(priceOpt, "/", triggerOpt, " trg.");
            } else {
                priceData = StringUtils.join(averagePriceOpt, "/", triggerOpt, " trg.");
            }

        } else {

            if (priceOpt != null && priceOpt.compareTo(BigDecimal.ZERO) > 0) {
                priceData = priceOpt.toString();
            } else {
                priceData = StringUtils.join(averagePriceOpt != null ? averagePriceOpt : 0.0);
            }
        }
        return priceData;
    }

    /**
     * Method used to invoke the broker api to get an user trade book & convert it's
     * response into Algotic trade book response
     *
     * @return List<OrderAndTradeBookResponse>
     */
    @Override
    public List<OrderAndTradeBookResponse> getTradeBook() {
        log.info("Method start -> getTradeBook");
        log.info("Fetching Motilal token");
        String token = userService.getMotilalToken(jwtHelper.getUserId());
        log.debug("Found auth token from motilal & trying to fetch order book...");
        TradeBookResponse orderBook = brokerApi.getTradeBook(token);
        List<OrderAndTradeBookResponse> orderAndTradeBookResponses = new ArrayList<>();

        if (StringUtils.equals("ERROR", orderBook.getStatus())) {
            log.error("Some error occurred while getting trade book [{}]", orderBook.getMessage());
            throw new AlgoticException(MotilalBrokerErrorCode.getErrorCodeByRefId(orderBook.getErrorCode()));
        }

        if (!CollectionUtils.isEmpty(orderBook.getData())) {
            log.info(
                    "Found trade book from motilal of size {}",
                    orderBook.getData().size());

            orderBook.getData().stream().forEach(tradeBookData -> {
                OrderAndTradeBookResponse orderAndTradeBookResponse = createOrderAndTradeBookResponse(tradeBookData);
                orderAndTradeBookResponses.add(orderAndTradeBookResponse);
            });
        }
        return orderAndTradeBookResponses;
    }

    /**
     *Square off of individual open positions for motilal oswal
     */
    @Override
    public AlgoticSquareOffResponse squareOff(AlgoticSquareOffRequest algoticSquareOffRequest) {
        String token = userService.getMotilalToken(jwtHelper.getUserId());
        log.info("fetching all position for squreOff");
        List<MotilalPositionData> positionlList = brokerApi.position(token);

        log.info("Filter position list related to token -> {}", algoticSquareOffRequest.getToken());
        MotilalPositionData positionData = positionlList.stream()
                .filter(p -> p.getSymbolToken()
                                .equalsIgnoreCase(
                                        algoticSquareOffRequest.getToken().toString())
                        && p.getProductName()
                                .equalsIgnoreCase(
                                        MotilalProductCode.getProductType(algoticSquareOffRequest.getProductCode())
                                                .toString()))
                .findFirst()
                .orElseThrow(() -> new AlgoticException(CommonErrorCode.DATA_NOT_FOUND));

        PlaceOrderRequest placeOrderRequest = convertToOrderRequest(positionData);

        log.info("Placing order for square off position.");
        PlaceOrderResponse squareOffResponse = brokerApi.placeOrder(placeOrderRequest, token);
        return new AlgoticSquareOffResponse(squareOffResponse.status(), squareOffResponse.message());
    }

    private PlaceOrderRequest convertToOrderRequest(MotilalPositionData positionData) {
        log.info("Converting motilal position data into placeOrderRequest for squareOff.");
        String buyOrSell;
        Integer quantity =
                Integer.parseInt(positionData.getBuyQuantity()) - Integer.parseInt(positionData.getSellQuantity());

        if (quantity < 0) {
            buyOrSell = TransactionType.BUY.name();
            quantity = quantity * -1;

        } else {
            buyOrSell = TransactionType.SELL.name();
        }

        return PlaceOrderRequest.builder()
                .buyorsell(buyOrSell)
                .triggerprice(0.0)
                .orderduration(Retention.DAY.name())
                .quantityinlot(quantity)
                .ordertype(MotilalPriceType.MARKET.getMotilalPriceType())
                .producttype(positionData.getProductName())
                .exchange(positionData.getExchange())
                .disclosedquantity(0)
                .symboltoken(Integer.valueOf(positionData.getSymbolToken()))
                .amoorder("N")
                .build();
    }

    /**
     *Square off all open position of motilal oswal broker
     */
    @Override
    public AlgoticSquareOffResponse squareOffAll() {
        log.info("Adapter calling for squareOffAll()");
        String token = userService.getMotilalToken(jwtHelper.getUserId());

        log.info("Fetching all position for squareOffAll.");
        List<MotilalPositionData> positionlList = brokerApi.position(token);

        log.info("Placing orders for squareOff of position data.");
        positionlList.parallelStream().forEach(motilalPositionData -> {
            PlaceOrderRequest placeOrderRequest = convertToOrderRequest(motilalPositionData);
            brokerApi.placeOrder(placeOrderRequest, token);
        });

        return new AlgoticSquareOffResponse("Completed", "SquareOff All Completed");
    }

    private void validateOrderRequest(OrderRequest orderRequest) {
        log.info("Validating Order Request...");

        if (!EnumUtils.isValidEnum(PriceType.class, orderRequest.getPriceType())) {
            throw new AlgoticException(CommonErrorCode.INVALID_PRICE_TYPE);
        }
        if (!EnumUtils.isValidEnum(AlgoticProductCode.class, orderRequest.getProductCode())) {
            throw new AlgoticException(CommonErrorCode.INVALID_PRODUCT_CODE);
        }
        if (!EnumUtils.isValidEnum(TransactionType.class, orderRequest.getTransactionType())) {
            throw new AlgoticException(CommonErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }
}
