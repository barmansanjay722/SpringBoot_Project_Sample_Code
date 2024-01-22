package com.algotic.services;

import com.algotic.model.request.TradeRequest;
import com.algotic.model.response.*;
import org.springframework.http.ResponseEntity;

public interface TradeService {

    ResponseEntity<TradeWebhookResponse> saveTrade(TradeRequest tradeRequest);

    ResponseEntity<GlobalMessageResponse> tradeActiveInactive(String id, String type);

    ResponseEntity<TradeSetupResponse> getTrades(int limit, int offset);

    ResponseEntity<GlobalMessageResponse> deleteTrade(String id);

    ResponseEntity<TradeExecutionResponse> tradeExecution(Integer limit, Integer offset);
}
