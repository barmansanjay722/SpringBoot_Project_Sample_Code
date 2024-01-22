package com.algotic.services;

import com.algotic.data.entities.PaperOrders;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.response.AlgoticSquareOffResponse;
import com.algotic.model.response.BookPositionResponse;
import com.algotic.model.response.HoldingAlgoticResponse;
import com.algotic.model.response.OrderAndTradeBookResponse;
import com.algotic.model.response.aliceblue.SquareOffResponse;
import java.util.List;

public interface PaperOrderService {
    PaperOrders paperOrder(OrderRequest orderRequest, String userId);

    List<OrderAndTradeBookResponse> paperOrderBook(String userId, String type);

    List<HoldingAlgoticResponse> holdingsPaperTrade(String userId);

    List<BookPositionResponse> positionBook(String userId);

    AlgoticSquareOffResponse paperOrderSquareOff(String token, String productCode, Double price, String userId);

    AlgoticSquareOffResponse paperOrderSquareOffAll();

    SquareOffResponse paperOrderScheduler(String userid);

    void paperOrderHoldingScheduler(String userId);

    void paperOrderInstrumentScheduler(PaperOrders paperOrders);
}
