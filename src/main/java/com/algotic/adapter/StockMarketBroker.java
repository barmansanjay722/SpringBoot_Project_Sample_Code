package com.algotic.adapter;

import com.algotic.constants.BrokerEnum;
import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.AlgoticModifyRequest;
import com.algotic.model.request.AlgoticSquareOffRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.response.AlgoticCancelOrderResponse;
import com.algotic.model.response.AlgoticSquareOffResponse;
import com.algotic.model.response.BookPositionResponse;
import com.algotic.model.response.HoldingAlgoticResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.OrderResponse;
import com.algotic.model.response.aliceblue.ModifyOrderResponse;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

/**
 *
 * This interface has only common actions for all types of Broker having common request & response object
 *
 */
@Component
public interface StockMarketBroker {

    OrderResponse placeOrder(OrderRequest orderRequest, String userId);

    List<HoldingAlgoticResponse> holdings(String token) throws NoSuchAlgorithmException;

    List<BookPositionResponse> bookPosition(String userId) throws NoSuchAlgorithmException;

    AlgoticCancelOrderResponse cancelOrder(AlgoticCancelOrderRequest algoticRequest, String userId);

    List<OrderAndTradeBookResponse> getOrderBook(String userId);

    List<OrderAndTradeBookResponse> getTradeBook();

    ModifyOrderResponse modifyOrder(AlgoticModifyRequest modifyRequest);

    BrokerEnum getBrokerName();

    AlgoticSquareOffResponse squareOff(AlgoticSquareOffRequest squareOffRequest);

    AlgoticSquareOffResponse squareOffAll();
}
