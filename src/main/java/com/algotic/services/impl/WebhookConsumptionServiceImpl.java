package com.algotic.services.impl;

import com.algotic.base.BrokerAdapterFactory;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
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
import com.algotic.data.entities.Trades;
import com.algotic.data.entities.Users;
import com.algotic.data.repositories.BrokersRepo;
import com.algotic.data.repositories.OrdersRepo;
import com.algotic.data.repositories.TradesRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.BusinessErrorCode;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.request.WebhookRequest;
import com.algotic.model.request.aliceblue.*;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.OrderResponse;
import com.algotic.model.response.aliceblue.*;
import com.algotic.services.PaperOrderService;
import com.algotic.services.UserService;
import com.algotic.services.WebhookConsumptionService;
import com.algotic.utils.AlgoticUtils;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebhookConsumptionServiceImpl implements WebhookConsumptionService {

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Value("${algoticEncryptionSalt}")
    private String algoticEncryptionSalt;

    @Value("${algoticEncryptionSecretKey}")
    private String algoticEncryptionSecretKey;

    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private TradesRepo tradesRepo;

    @Autowired
    private BrokerAdapterFactory adapterFactory;

    @Autowired
    private UserService userService;

    @Value("${apiKey}")
    private String apiKey;

    @Value("${paperTradeUserId}")
    private String paperTradeUserId;

    @Autowired
    private PaperOrderService paperOrderService;

    @Autowired
    private OrdersRepo ordersRepo;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private BrokersRepo brokerRepo;

    @Override
    public ResponseEntity<GlobalMessageResponse> webhookConsume(WebhookRequest webhookRequest, String webhookUrlData) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Webhook consume", "consume webhook and get userId and trade id", null, null));

            byte[] actualByte = Base64.getUrlDecoder().decode(webhookUrlData);
            String decodeUrl = new String(actualByte);
            String decryptWebhookData =
                    AlgoticUtils.decrypt(decodeUrl, algoticEncryptionSecretKey, algoticEncryptionSalt);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog(
                            "Decrypt Webhook Data", AlgoticUtils.objectToJsonString(decryptWebhookData), null, null));
            String[] getCustomerTradeId = decryptWebhookData.split("@");
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("customer Id", AlgoticUtils.objectToJsonString(getCustomerTradeId), null, null));
            ArrayList<String> getDataList = new ArrayList<>();
            for (String a : getCustomerTradeId) {
                getDataList.add(a);
            }
            String userId = getDataList.get(0);
            String tradeId = getDataList.get(1);

            Users userData = usersRepo.findByID(userId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("User Data", AlgoticUtils.objectToJsonString(userData), null, null));
            if (userData == null) {
                throw new AlgoticException(CommonErrorCode.CUSTOMER_NOT_EXISTS);
            }

            Trades tradesData = tradesRepo.findTradePresentAndActive(tradeId);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Trades Data", AlgoticUtils.objectToJsonString(tradesData), null, null));
            if (tradesData == null) {
                throw new AlgoticException(BusinessErrorCode.TRADE_IS_NOT_ACTIVE);
            }
            if (tradesData.getTradeType().equalsIgnoreCase(TradeType.LIVE.name())) {
                String token = userService.getAliceBlueToken(userId);
                log.info(logConfig
                        .getLogHandler()
                        .getInfoLog("Token", AlgoticUtils.objectToJsonString(token), userId, null));

                if (webhookRequest.getTransactionType().equalsIgnoreCase("BUY")) {
                    executeAutomaticTrades(userId, tradesData, webhookRequest.getTransactionType());
                } else if (webhookRequest.getTransactionType().equalsIgnoreCase("SELL")) {
                    placeMarketOrder(userId, tradesData, webhookRequest.getTransactionType());
                    List<Orders> orders = ordersRepo.getOrderDetails(tradeId);
                    log.info(logConfig
                            .getLogHandler()
                            .getInfoLog("Orders List", AlgoticUtils.objectToJsonString(orders), null, null));
                    for (Orders order : orders) {
                        if (order.getPriceType().equalsIgnoreCase("SL")
                                || order.getPriceType().equalsIgnoreCase("L")) {
                            String brokerName = brokerRepo.findBrokerNameByCustomerId(userId);
                            AlgoticCancelOrderRequest cancelOrderReq = new AlgoticCancelOrderRequest(
                                    order.getExchange(), order.getNestOrderNumber(), order.getTradingSymbol());
                            adapterFactory
                                    .getAdapter(BrokerEnum.getBrokerEnum(brokerName))
                                    .cancelOrder(cancelOrderReq, userId);
                        }
                    }
                }
            } else if (tradesData.getTradeType().equalsIgnoreCase(TradeType.PAPER.name())) {
                OrderRequest placeOrderRequest =
                        generateOrderRequest(tradesData, webhookRequest.getTransactionType(), PriceType.MARKET);
                Orders orders = saveOrder(placeOrderRequest, userId, tradeId, tradesData.getTradeType());
                OrderRequest request = new OrderRequest();
                request.setProductCode(tradesData.getOrderType());
                request.setToken(tradesData.getToken());
                request.setPriceType(PriceType.MARKET.name());
                request.setQuantity(tradesData.getLotSize());
                request.setTradingSymbol(tradesData.getTradingSymbol());
                request.setInstrumentName(tradesData.getInstrumentName());
                request.setTradeType(tradesData.getTradeType());
                request.setExchange(tradesData.getExchange());
                request.setTransactionType(webhookRequest.getTransactionType());
                PaperOrders paperOrders = paperOrderService.paperOrder(request, userId);
                orders.setNestOrderNumber(paperOrders.getId());
                orders.setStatus(PlaceOrder.PLACED.name());
                ordersRepo.save(orders);

            } else {
                throw new AlgoticException(CommonErrorCode.INVALID_TRADE_TYPE);
            }
            log.info(logConfig.getLogHandler().getInfoLog("WebHook Consume", "Webhook Consumption", null, null));
            return new ResponseEntity<>(new GlobalMessageResponse("Order Placed Successfully"), HttpStatus.OK);
        } catch (AlgoticException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            ex.getMessage(),
                            "error in webhook consume",
                            null,
                            CommonErrorCode.INTERNAL_SERVER_ERROR.getErrorCode()));
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void placeStopLossOrder(String userId, Trades trade, Double marketPrice) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Stop loss order", "Place the stop loss order by userId and trade", userId, null));
        String transactionType = TransactionType.SELL.name();
        OrderRequest placeOrderRequest = generateOrderRequest(trade, transactionType, PriceType.STOP_LOSS);
        Double price = marketPrice - trade.getStopLossPrice();
        placeOrderRequest.setTriggerPrice(price);
        placeOrder(placeOrderRequest, userId, trade.getId(), trade.getTradeType());
    }

    private void placeTargetProfitOrder(String userId, Trades trade, Double marketPrice) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Stop Target Profit order", "Place the Target Profit Order by userId and trade", userId, null));
        String transactionType = TransactionType.SELL.name();

        OrderRequest placeOrderRequest = generateOrderRequest(trade, transactionType, PriceType.LIMIT);
        Double price = marketPrice + trade.getTargetProfit();
        placeOrderRequest.setPrice(price);
        placeOrder(placeOrderRequest, userId, trade.getId(), trade.getTradeType());
    }

    private String placeMarketOrder(String userId, Trades trades, String transactionType) {
        try {
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("Place Market Order", "PLace the order of type market", null, null));
            OrderRequest placeOrderRequest = generateOrderRequest(trades, transactionType, PriceType.MARKET);
            log.info(logConfig
                    .getLogHandler()
                    .getInfoLog("PLace order request", AlgoticUtils.objectToJsonString(placeOrderRequest), null, null));
            String nestOrderNumber = placeOrder(placeOrderRequest, userId, trades.getId(), trades.getTradeType());
            return nestOrderNumber;
        } catch (AlgoticException e) {
            throw e;
        } catch (Exception ex) {
            log.error(logConfig
                    .getLogHandler()
                    .getErrorLog(
                            ex.getMessage(),
                            "error in webhook consume",
                            null,
                            CommonErrorCode.INTERNAL_SERVER_ERROR.getErrorCode()));
            throw new AlgoticException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String placeOrder(OrderRequest placeOrderRequest, String userId, String tradeId, String tradeType) {
        // TODO
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Method start -> Place Order", "Place the order by user with trade Id", null, null));
        Orders orders = saveOrder(placeOrderRequest, userId, tradeId, tradeType);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Orders saved into DB", AlgoticUtils.objectToJsonString(orders), userId, null));
        BrokerEnum brokerName = BrokerEnum.getBrokerEnum(brokerRepo.findBrokerNameByCustomerId(userId));
        Integer brokerId = brokerRepo.findbrokerId(brokerName.getBrokerName());
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Currently user associate with boker" + brokerName, null, userId, null));
        log.info("Broker Name -> {}", brokerName);
        OrderResponse orderResponse = adapterFactory.getAdapter(brokerName).placeOrder(placeOrderRequest, userId);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Place Order Data", AlgoticUtils.objectToJsonString(orderResponse), userId, null));

        orders.setNestOrderNumber(orderResponse.getOrderRefNumber());
        orders.setBrokerId(brokerId);
        orders.setStatus(orderResponse.getOrderStatus());
        if (orderResponse.getErrorMessage() != null) {
            orders.setErrorMessage(orderResponse.getErrorMessage());
        } else {
            orders.setErrorMessage(orderResponse.getErrorMsge());
        }
        ordersRepo.save(orders);
        return orderResponse.getOrderRefNumber();
    }

    private OrderRequest generateOrderRequest(Trades trades, String transactionType, PriceType priceType) {
        log.info(logConfig.getLogHandler().getInfoLog("Place order", "Place the order with alice blue", null, null));
        OrderRequest order = new OrderRequest();
        order.setComplexity(Complexity.REGULAR.name());
        order.setTradingSymbol(trades.getTradingSymbol());
        order.setTransactionType(transactionType.toUpperCase());
        order.setPriceType(priceType.name());
        order.setQuantity(trades.getLotSize());
        order.setProductCode(trades.getOrderType());
        order.setExchange(trades.getExchange());
        order.setToken(trades.getToken());
        order.setDiscloseQuantity(0);

        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Order Request", AlgoticUtils.objectToJsonString(order), null, null));
        return order;
    }

    private void executeAutomaticTrades(String userId, Trades trade, String transactionType) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Execute Automatic Trades",
                        "Exceute the Automatic trades by user , trade and transaction type ",
                        userId,
                        null));
        String nestOrderNumber = placeMarketOrder(userId, trade, transactionType);
        Double marketPrice = getPriceFromMarketOrder(nestOrderNumber, userId);
        if (trade.getTargetProfit() != null && trade.getTargetProfit() > 0.0) {
            placeTargetProfitOrder(userId, trade, marketPrice);
        }

        if (trade.getStopLossPrice() != null && trade.getStopLossPrice() > 0) {
            placeStopLossOrder(userId, trade, marketPrice);
        }
    }

    private Double getPriceFromMarketOrder(String nestOrderNumber, String userId) {
        String price = null;
        BrokerEnum brokerName = BrokerEnum.getBrokerEnum(brokerRepo.findBrokerNameByCustomerId(userId));
        List<OrderAndTradeBookResponse> orderAndTradeBookResponseList =
                adapterFactory.getAdapter(brokerName).getOrderBook(userId);
        for (OrderAndTradeBookResponse orderBookResponse : orderAndTradeBookResponseList) {
            if (orderBookResponse.getNestOrderNumber().equals(nestOrderNumber)) {
                price = orderBookResponse.getPrice();
            }
        }
        return Double.parseDouble(price);
    }

    private Orders saveOrder(OrderRequest orderRequest, String userId, String tradeId, String tradeType) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Save Order", "Save Order with user id and trade id", userId, null));
        Orders orders = new Orders();
        orders.setComplexity(orderRequest.getComplexity());
        orders.setTradingSymbol(orderRequest.getTradingSymbol());
        orders.setTransactionType(orderRequest.getTransactionType());
        orders.setTriggerPrice(orderRequest.getTriggerPrice());
        orders.setRetention(Retention.DAY.name());
        orders.setQuantity(orderRequest.getQuantity());
        orders.setPrice(orderRequest.getPrice());
        orders.setPriceType(orderRequest.getPriceType());
        orders.setProductCode(orderRequest.getProductCode());
        orders.setExchange(orderRequest.getExchange());
        orders.setDiscloseQuantity(orderRequest.getDiscloseQuantity());
        orders.setUserId(userId);
        orders.setNestOrderNumber(null);
        orders.setOrderType(OrderTriggerType.AUTOMATIC.name());
        orders.setStatus(PlaceOrder.CREATED.name());
        orders.setCreatedAt(new Date());
        orders.setTradeId(tradeId);
        orders.setTradeType(tradeType);
        orders.setToken(orderRequest.getToken());
        Orders dataOrders = ordersRepo.save(orders);
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Orders Data", AlgoticUtils.objectToJsonString(dataOrders), userId, null));

        return orders;
    }
}
