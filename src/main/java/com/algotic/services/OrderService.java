package com.algotic.services;

import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.AlgoticModifyRequest;
import com.algotic.model.request.AlgoticSquareOffRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.response.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    String aliceBlueToken(String userId);

    ResponseEntity<List<HoldingAlgoticResponse>> getHoldings();

    ResponseEntity<List<BookPositionResponse>> getPositions();

    ResponseEntity<GlobalMessageResponse> placeOrder(OrderRequest orderRequest);

    ResponseEntity<List<OrderAndTradeBookResponse>> orderBook(String type);

    ResponseEntity<AlgoticCancelOrderResponse> cancelOrder(AlgoticCancelOrderRequest algoticRequest);

    ResponseEntity<AlgoticSquareOffResponse> squareOffAll() throws NoSuchAlgorithmException;

    ResponseEntity<PortfolioResponse> getPortfolioData();

    ResponseEntity<GlobalMessageResponse> modifyOrder(AlgoticModifyRequest modifyRequest);

    ResponseEntity<AlgoticSquareOffResponse> squareOff(AlgoticSquareOffRequest algoticSquareOffRequest);
}
