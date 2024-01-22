package com.algotic.adapter.impl;

import com.algotic.adapter.StockMarketBroker;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.broker.api.AliceBlueBrokerApi;
import com.algotic.config.JwtHelper;
import com.algotic.constants.AlgoticProductCode;
import com.algotic.constants.BrokerEnum;
import com.algotic.constants.Exchange;
import com.algotic.constants.PlaceOrder;
import com.algotic.constants.PriceType;
import com.algotic.constants.Retention;
import com.algotic.constants.TradeType;
import com.algotic.constants.TransactionType;
import com.algotic.constants.aliceblue.AliceBluePriceType;
import com.algotic.constants.aliceblue.AliceBlueProductCode;
import com.algotic.constants.aliceblue.Complexity;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.AlgoticModifyRequest;
import com.algotic.model.request.AlgoticSquareOffRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.request.aliceblue.CancelOrderRequest;
import com.algotic.model.request.aliceblue.ModifyOrderRequest;
import com.algotic.model.request.aliceblue.PlaceOrderRequest;
import com.algotic.model.request.aliceblue.PositionBookRequest;
import com.algotic.model.request.aliceblue.SquareOffRequest;
import com.algotic.model.response.AlgoticCancelOrderResponse;
import com.algotic.model.response.AlgoticSquareOffResponse;
import com.algotic.model.response.BookPositionResponse;
import com.algotic.model.response.HoldingAlgoticResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.OrderResponse;
import com.algotic.model.response.aliceblue.*;
import com.algotic.services.UserService;
import com.algotic.utils.AlgoticUtils;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 *
 * This adapter class is used to defined all methods of StockMarkerBroker and if
 * any others related to Aliceblue but having common request & Response object
 * Will do validation of request Object Will do conversation from common request
 * to AliceBluespecifc request object Will do conversion from AliceBlueSpecific
 * Response to common Response object Will call AliceblueApi
 *
 */
@Slf4j
@Component
public class AliceBlueAdapter implements StockMarketBroker {

    @Autowired
    private AliceBlueBrokerApi brokerApi;

    @Autowired
    private UserService userService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    /**
     * Method used to convert into alice blue specific order request/ response and
     * invoke the broker api class to place an order
     */
    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Place Order via Alice blue broker", orderRequest.toString(), userId, null));
        validateOrderRequest(orderRequest);

        log.info("Fetching Alice Blue token");
        String token = userService.getAliceBlueToken(userId);
        log.info("Found token form Alice Blue | Placing order using alice blue api.");

        PlaceOrderResponse placeOrderRes = brokerApi.placeOrder(generateAliceBlueOrderRequest(orderRequest), token);
        return convertIntoOrderResponse(placeOrderRes);
    }

    private OrderResponse convertIntoOrderResponse(PlaceOrderResponse aliceBlueOrderRes) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Coverting order response into algotic common response",
                        AlgoticUtils.objectToJsonString(aliceBlueOrderRes),
                        null,
                        null));
        OrderResponse algoticOrderRes = new OrderResponse();
        algoticOrderRes.setOrderRefNumber(aliceBlueOrderRes.getNestOrderNumber());

        if (aliceBlueOrderRes.getStatus().contains("Not_Ok")) {
            log.error("Some error occurred while placing order using alice blue api");
            algoticOrderRes.setOrderStatus(PlaceOrder.FAILED.name());
            algoticOrderRes.setException(true);
            algoticOrderRes.setErrorCode(BusinessErrorCode.SOMETHING_WENT_WRONG);
        } else {
            algoticOrderRes.setOrderStatus(PlaceOrder.PLACED.name());
        }

        algoticOrderRes.setErrorMessage(aliceBlueOrderRes.getErrorMessage());
        algoticOrderRes.setErrorMsge(aliceBlueOrderRes.getErrorMsge());

        if ((aliceBlueOrderRes.getErrorMessage() != null
                        && aliceBlueOrderRes.getErrorMessage().contains("EDIS"))
                || (aliceBlueOrderRes.getErrorMsge() != null
                        && aliceBlueOrderRes.getErrorMsge().contains("EDIS"))) {
            algoticOrderRes.setException(true);
            algoticOrderRes.setErrorCode(BusinessErrorCode.EDIS_AUTHORIZATION_REQUIRED);
        }

        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Succesfully converted into algotic response",
                        AlgoticUtils.objectToJsonString(algoticOrderRes),
                        null,
                        null));

        return algoticOrderRes;
    }

    private void validateOrderRequest(OrderRequest orderRequest) {
        log.info("Validating Order Request...");

        if (!EnumUtils.isValidEnum(PriceType.class, orderRequest.getPriceType())) {
            throw new AlgoticException(CommonErrorCode.INVALID_PRICE_TYPE);
        }
        if (!EnumUtils.isValidEnum(
                TransactionType.class, orderRequest.getTransactionType().toUpperCase())) {
            throw new AlgoticException(CommonErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

    @Override
    public List<HoldingAlgoticResponse> holdings(String userId) throws NoSuchAlgorithmException {
        String token = userService.getAliceBlueToken(userId);
        List<HoldingValue> holdinglist = brokerApi.holdings(token).getHoldingValue();
        List<HoldingAlgoticResponse> holdingAlgoticResponseList = new ArrayList<>();

        for (HoldingValue holdingData : holdinglist) {
            holdingAlgoticResponseList.add(createGlobleHolding(holdingData));
        }
        return holdingAlgoticResponseList;
    }

    private HoldingAlgoticResponse createGlobleHolding(HoldingValue holdingValue) {

        HoldingAlgoticResponse holdingAlgoticResponse = new HoldingAlgoticResponse();
        holdingAlgoticResponse.setQuantity(holdingValue.getHoldQuantity());
        holdingAlgoticResponse.setTradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()));
        if (holdingValue.getNseTradingSymbol().equalsIgnoreCase("0")) {
            holdingAlgoticResponse.setInstrumentName(holdingValue.getBseTradingSymbol());
        } else {
            holdingAlgoticResponse.setInstrumentName(holdingValue.getNseTradingSymbol());
        }

        if (StringUtils.isNotBlank(holdingValue.getNseTradingSymbol())
                && !holdingValue.getNseTradingSymbol().equalsIgnoreCase("0")) {
            holdingAlgoticResponse.setTradingSymbol(holdingValue.getNseTradingSymbol());
        } else if (StringUtils.isNotBlank(holdingValue.getBseTradingSymbol())
                && !holdingValue.getBseTradingSymbol().equalsIgnoreCase("0")) {
            holdingAlgoticResponse.setTradingSymbol(holdingValue.getBseTradingSymbol());
        }

        holdingAlgoticResponse.setLtp(holdingValue.getLastTradePrice());
        holdingAlgoticResponse.setQuantity(holdingValue.getSellableQty());
        holdingAlgoticResponse.setOrderType(holdingValue.getProductCode());
        holdingAlgoticResponse.setPdc(holdingValue.getPdc());

        if (StringUtils.isNotBlank(holdingValue.getExchangeSegmentOne())) {
            holdingAlgoticResponse.setExchange(holdingValue.getExchangeSegmentOne());
        } else if (StringUtils.isNotBlank(holdingValue.getExchangeSegmentTwo())) {
            holdingAlgoticResponse.setExchange(holdingValue.getExchangeSegmentTwo());
        }

        if (holdingAlgoticResponse.getExchange().equalsIgnoreCase(Exchange.NSE.name())
                || holdingAlgoticResponse.getExchange().equalsIgnoreCase(Exchange.BSE.name())) {
            holdingAlgoticResponse.setIsStock(true);
        } else {
            holdingAlgoticResponse.setIsStock(false);
        }

        holdingAlgoticResponse.setPrice(holdingValue.getPrice());

        if (StringUtils.isNotBlank(holdingValue.getTokenOne())
                && !holdingValue.getTokenOne().equalsIgnoreCase("0")) {
            holdingAlgoticResponse.setToken(holdingValue.getTokenOne());
        } else if (StringUtils.isNotBlank(holdingValue.getTokenTwo())
                && !holdingValue.getTokenTwo().equalsIgnoreCase("0")) {
            holdingAlgoticResponse.setToken(holdingValue.getTokenTwo());
        }
        return holdingAlgoticResponse;
    }

    @Override
    public List<BookPositionResponse> bookPosition(String userId) throws NoSuchAlgorithmException {
        try {
            String token = userService.getAliceBlueToken(userId);

            PositionBookRequest positionBookRequest = new PositionBookRequest();
            positionBookRequest.setRetention(Retention.NET.name());
            List<PositionBookResponse> positionBookResponseList = brokerApi.bookPosition(positionBookRequest, token);

            List<BookPositionResponse> bookPositionResponseList = new ArrayList<>();

            if (positionBookResponseList != null) {
                for (PositionBookResponse positionBookResponse : positionBookResponseList) {
                    if (Float.parseFloat(positionBookResponse.getNetQty()) != 0) {
                        BookPositionResponse bookPositionResponse = createBookPositionResponse(positionBookResponse);
                        bookPositionResponseList.add(bookPositionResponse);
                    }
                }

                for (PositionBookResponse positionBookResponse : positionBookResponseList) {
                    if (Float.parseFloat(positionBookResponse.getNetQty()) == 0) {
                        BookPositionResponse bookPositionResponse = createBookPositionResponse(positionBookResponse);
                        bookPositionResponseList.add(bookPositionResponse);
                    }
                }
            }
            if (bookPositionResponseList.isEmpty()) {
                throw new AlgoticException(CommonErrorCode.DATA_NOT_FOUND);
            }

            return bookPositionResponseList;
        } catch (AlgoticException e) {
            throw new AlgoticException(e.getErrorCode());
        } catch (Exception e) {
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private BookPositionResponse createBookPositionResponse(PositionBookResponse positionBookResponse) {
        BookPositionResponse bookPositionResponse = new BookPositionResponse();

        bookPositionResponse.setTradingSymbol(positionBookResponse.getTsym());
        bookPositionResponse.setInstrumentName(
                StringUtils.isNotEmpty(positionBookResponse.getCompanyName())
                        ? positionBookResponse.getCompanyName()
                        : positionBookResponse.getTsym());
        bookPositionResponse.setTradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()));
        bookPositionResponse.setOrderType(positionBookResponse.getPCode());
        bookPositionResponse.setQuantity(positionBookResponse.getNetQty());
        bookPositionResponse.setLastTradePrice(AlgoticUtils.commaFilter(positionBookResponse.getLTP()));
        bookPositionResponse.setBuyAverage(AlgoticUtils.commaFilter(positionBookResponse.getBuyAvgPrc()));
        bookPositionResponse.setNetBuyAvgPrice(AlgoticUtils.commaFilter(positionBookResponse.getNetBuyAvgPrc()));
        bookPositionResponse.setNetSellAvgPrice(AlgoticUtils.commaFilter(positionBookResponse.getNetSellAvgPrc()));
        bookPositionResponse.setSellAverage(AlgoticUtils.commaFilter(positionBookResponse.getSellAvgPrc()));

        if (positionBookResponse.getRealisedProfitLoss() == null
                || positionBookResponse.getRealisedProfitLoss().isEmpty()) {
            bookPositionResponse.setProfitAndLoss(
                    AlgoticUtils.commaFilter(positionBookResponse.getUnrealisedProfitLoss()));
        } else if (positionBookResponse.getUnrealisedProfitLoss() == null
                || positionBookResponse.getUnrealisedProfitLoss().isEmpty()) {
            bookPositionResponse.setProfitAndLoss(
                    AlgoticUtils.commaFilter(positionBookResponse.getRealisedProfitLoss()));
        } else {
            double totalPl = Double.parseDouble(
                            positionBookResponse.getRealisedProfitLoss().replace(",", ""))
                    + Double.parseDouble(
                            positionBookResponse.getUnrealisedProfitLoss().replace(",", ""));
            bookPositionResponse.setProfitAndLoss(AlgoticUtils.commaFilter(String.valueOf(totalPl)));
        }

        bookPositionResponse.setExchange(positionBookResponse.getExchange());
        bookPositionResponse.setIsStock(bookPositionResponse.getExchange().equalsIgnoreCase(Exchange.NSE.name())
                || bookPositionResponse.getExchange().equalsIgnoreCase(Exchange.BSE.name()));
        bookPositionResponse.setToken(positionBookResponse.getToken());
        bookPositionResponse.setBlQty(positionBookResponse.getBLQty());
        bookPositionResponse.setNetQuantity(positionBookResponse.getNetQty());
        bookPositionResponse.setNetBuyQty(positionBookResponse.getNetbuyqty());
        bookPositionResponse.setNetSellQty(positionBookResponse.getNetsellqty());
        return bookPositionResponse;
    }

    private PlaceOrderRequest generateAliceBlueOrderRequest(OrderRequest orderRequest) {
        log.info("Generating alice blue specific request");
        return PlaceOrderRequest.builder()
                .complexity(Complexity.REGULAR.name())
                .tradingSymbol(orderRequest.getTradingSymbol())
                .transactionType(orderRequest.getTransactionType())
                .triggerPrice(orderRequest.getTriggerPrice())
                .retention(Retention.DAY.name())
                .quantity(orderRequest.getQuantity())
                .price(orderRequest.getPrice())
                .priceType(AliceBluePriceType.getPriceType(orderRequest.getPriceType())
                        .name())
                .productCode(AliceBlueProductCode.getProductCode(orderRequest.getProductCode())
                        .name())
                .exchange(orderRequest.getExchange())
                .discloseQuantity(0)
                .symbolId(orderRequest.getToken())
                .build();
    }

    @Override
    public BrokerEnum getBrokerName() {
        return BrokerEnum.ALICE_BLUE;
    }

    @Override
    public AlgoticCancelOrderResponse cancelOrder(AlgoticCancelOrderRequest algoticRequest, String userId) {
        String token = userService.getAliceBlueToken(userId);
        CancelOrderResponse cancelOrderResponse =
                brokerApi.cancelOrder(createCancelOrderRequest(algoticRequest), token);

        AlgoticCancelOrderResponse orderResponse = new AlgoticCancelOrderResponse();

        if (cancelOrderResponse.getStatus().equals("Not_Ok")) {
            orderResponse.setException(true);
        }
        orderResponse.setStatus(cancelOrderResponse.getStatus());
        log.info("alice blue cancel order nestOrderNumber" + cancelOrderResponse.getNestOrderNumber());
        if (cancelOrderResponse.getNestOrderNumber() == null) {
            orderResponse.setNestOrderNumber(null);
        } else {
            orderResponse.setNestOrderNumber(
                    cancelOrderResponse.getNestOrderNumber().toString());
        }
        return orderResponse;
    }

    private CancelOrderRequest createCancelOrderRequest(AlgoticCancelOrderRequest algoticRequest) {
        return new CancelOrderRequest(
                algoticRequest.getExchange(), algoticRequest.getNestOrderNumber(), algoticRequest.getTradingSymbol());
    }

    @Override
    public List<OrderAndTradeBookResponse> getOrderBook(String userId) {
        log.info("Fetching auth token to complete alice blue request");
        String token = userService.getAliceBlueToken(userId);
        log.debug("Found auth token from alice blue & trying to fetch order book");
        List<OrderBookResponse> orderBooks = brokerApi.getOrderBook(token);

        List<OrderAndTradeBookResponse> orderAndTradeBookResponses = new ArrayList<>();

        if (orderBooks != null) {
            log.debug("Found order book of size {}", orderBooks.size());

            for (OrderBookResponse orderBook : orderBooks) {
                OrderAndTradeBookResponse orderAndTradeBookResponse = createOrderAndTradeBookResponse(orderBook);
                orderAndTradeBookResponses.add(orderAndTradeBookResponse);
            }
        }
        return orderAndTradeBookResponses;
    }

    private OrderAndTradeBookResponse createOrderAndTradeBookResponse(OrderBookResponse orderBookResponse) {
        OrderAndTradeBookResponse orderAndTradeBookResponse = new OrderAndTradeBookResponse();
        orderAndTradeBookResponse.setTime(orderBookResponse.getOrderedTime().split(" ")[1]);
        orderAndTradeBookResponse.setTradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()));
        orderAndTradeBookResponse.setType(getTransactionType(orderBookResponse.getTransactionType()));
        orderAndTradeBookResponse.setTradingSymbol(orderBookResponse.getTradingSymbol());
        orderAndTradeBookResponse.setInstrument(orderBookResponse.getInstrumentName());
        orderAndTradeBookResponse.setPrice(getPriceData(orderBookResponse));

        orderAndTradeBookResponse.setProduct(getProductData(orderBookResponse));
        orderAndTradeBookResponse.setQuantity(orderBookResponse.getQuantity());
        orderAndTradeBookResponse.setStatus(AlgoticUtils.convertToPascalCase(orderBookResponse.getStatus()));
        orderAndTradeBookResponse.setNestOrderNumber(orderBookResponse.getNestOrderNumber());
        orderAndTradeBookResponse.setExchange(orderBookResponse.getExchange());
        return orderAndTradeBookResponse;
    }

    private String getTransactionType(String transactionType) {
        if (transactionType.equalsIgnoreCase("B")) {
            return AlgoticUtils.convertToPascalCase(TransactionType.BUY.name());
        } else {
            return AlgoticUtils.convertToPascalCase(TransactionType.SELL.name());
        }
    }

    private String getPriceData(OrderBookResponse orderBookResponse) {
        if (orderBookResponse.getTriggerPrice() != null && Float.parseFloat(orderBookResponse.getTriggerPrice()) > 0) {
            return (Float.parseFloat(orderBookResponse.getPrice() != null ? orderBookResponse.getPrice() : "0") > 0
                    ? (Float.valueOf(orderBookResponse.getPrice()) + "/"
                            + Float.valueOf(orderBookResponse.getTriggerPrice()) + " trg.")
                    : Float.valueOf(orderBookResponse.getAveragePrice()) + "/"
                            + Float.valueOf(orderBookResponse.getTriggerPrice()) + " trg.");
        } else {
            return Float.parseFloat(orderBookResponse.getPrice() != null ? orderBookResponse.getPrice() : "0") > 0
                    ? String.valueOf(Float.parseFloat(orderBookResponse.getPrice()))
                    : String.valueOf(Float.parseFloat(
                            orderBookResponse.getAveragePrice() != null ? orderBookResponse.getAveragePrice() : "0"));
        }
    }

    private String getProductData(OrderBookResponse orderBookResponse) {
        if (orderBookResponse.getPriceType().equalsIgnoreCase("MKT")) {
            return orderBookResponse.getProductCode() + " / MKT";
        } else if (orderBookResponse.getPriceType().equalsIgnoreCase("L")) {
            return orderBookResponse.getProductCode() + " / L";
        } else if (orderBookResponse.getPriceType().equalsIgnoreCase("SL")) {
            return orderBookResponse.getProductCode() + " / SL";
        }
        return "";
    }

    @Override
    public List<OrderAndTradeBookResponse> getTradeBook() {
        log.info("Method start -> getTradeBook, Fetching token from alice blue...");

        String token = userService.getAliceBlueToken(jwtHelper.getUserId());

        log.info("Calling alice blue broker api to fetch user trade");
        List<TradeBookResponse> tradeBookData = brokerApi.getTradeBook(token);

        List<OrderAndTradeBookResponse> tradeResponse = new ArrayList<>();

        if (!CollectionUtils.isEmpty(tradeBookData)) {
            log.info("Converting alice blue response to algotic specific response");
            for (TradeBookResponse tradeBookResponse : tradeBookData) {
                OrderAndTradeBookResponse orderAndTradeBookResponse = new OrderAndTradeBookResponse();

                orderAndTradeBookResponse.setTime(tradeBookResponse.getTime().split(" ")[1]);
                orderAndTradeBookResponse.setTradeType(AlgoticUtils.convertToPascalCase(TradeType.LIVE.name()));

                if (tradeBookResponse.getTransactionType().equalsIgnoreCase("B")) {
                    orderAndTradeBookResponse.setType(AlgoticUtils.convertToPascalCase(TransactionType.BUY.name()));
                } else {
                    orderAndTradeBookResponse.setType(AlgoticUtils.convertToPascalCase(TransactionType.SELL.name()));
                }

                orderAndTradeBookResponse.setInstrument(tradeBookResponse.getTsym());
                orderAndTradeBookResponse.setTradingSymbol(tradeBookResponse.getTsym());
                orderAndTradeBookResponse.setProduct(getTradeBookProductData(tradeBookResponse));
                orderAndTradeBookResponse.setQuantity(tradeBookResponse.getQuantity());
                orderAndTradeBookResponse.setStatus(AlgoticUtils.convertToPascalCase(tradeBookResponse.getStatus()));
                orderAndTradeBookResponse.setPrice(
                        tradeBookResponse.getPrice() != null
                                ? String.valueOf(Float.parseFloat(tradeBookResponse.getPrice()))
                                : "");
                orderAndTradeBookResponse.setExchange(tradeBookResponse.getExchange());
                tradeResponse.add(orderAndTradeBookResponse);
            }
        }

        return tradeResponse;
    }

    private String getTradeBookProductData(TradeBookResponse tradeBookResponse) {
        if (tradeBookResponse.getPriceType().equalsIgnoreCase("MKT")) {
            return tradeBookResponse.getProductCode() + " / MKT";
        } else if (tradeBookResponse.getPriceType().equalsIgnoreCase("L")) {
            return tradeBookResponse.getProductCode() + " / L";
        }
        return "";
    }

    @Override
    public ModifyOrderResponse modifyOrder(AlgoticModifyRequest modifyRequest) {
        validateModifyRequest(modifyRequest);
        String token = userService.getAliceBlueToken(jwtHelper.getUserId());

        ModifyOrderRequest modifyOrderRequest = generateModifyOrder(modifyRequest);
        return brokerApi.modifyOrder(modifyOrderRequest, token);
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

    private ModifyOrderRequest generateModifyOrder(AlgoticModifyRequest modifyRequest) {
        return ModifyOrderRequest.builder()
                .tradingSymbol(modifyRequest.getTradingSymbol())
                .transactionType(modifyRequest.getTransactionType())
                .triggerPrice(modifyRequest.getTriggerPrice())
                .quantity(modifyRequest.getQuantity())
                .filledQuantity(modifyRequest.getFilledQuantity())
                .nestOrderNumber(modifyRequest.getNestOrderNumber())
                .price(modifyRequest.getPrice())
                .priceType(AliceBluePriceType.getPriceType(modifyRequest.getPriceType())
                        .name())
                .productCode(AliceBlueProductCode.getProductCode(modifyRequest.getProductCode())
                        .name())
                .exchange(modifyRequest.getExchange())
                .disclosedQuantity(0)
                .build();
    }

    @Override
    public AlgoticSquareOffResponse squareOff(AlgoticSquareOffRequest algoticSquareOffRequest) {
        SquareOffRequest squareOffRequest = createSquareOffRequest(algoticSquareOffRequest);
        String token = userService.getAliceBlueToken(jwtHelper.getUserId());
        SquareOffResponse squareOffResponse = brokerApi.squareOff(squareOffRequest, token);
        log.info("Successfully squareOff order. Product code -> {}", squareOffRequest.getProductCode());
        return new AlgoticSquareOffResponse(squareOffResponse.getStatus(), squareOffResponse.getMessage());
    }

    private SquareOffRequest createSquareOffRequest(AlgoticSquareOffRequest algoticSquareOffRequest) {
        log.info("Creating aliceBlue specific square off request.");
        SquareOffRequest squareOffRequest = new SquareOffRequest();
        squareOffRequest.setToken(algoticSquareOffRequest.getToken());
        squareOffRequest.setProductCode(algoticSquareOffRequest.getProductCode());
        return squareOffRequest;
    }

    @Override
    public AlgoticSquareOffResponse squareOffAll() {
        String token = userService.getAliceBlueToken(jwtHelper.getUserId());
        SquareOffResponse squareOffResponse = brokerApi.squareOffAll(token);
        log.info("Successfully squre off all orders.");
        return new AlgoticSquareOffResponse(squareOffResponse.getStatus(), squareOffResponse.getMessage());
    }
}
