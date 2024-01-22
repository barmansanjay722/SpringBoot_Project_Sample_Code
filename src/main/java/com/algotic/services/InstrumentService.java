package com.algotic.services;

import com.algotic.model.request.AlgoticInstrumentRequest;
import com.algotic.model.response.*;
import com.algotic.model.response.aliceblue.Result;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface InstrumentService {

    ResponseEntity<List<InstrumentSearchAllStockResponse>> getStockNames(
            String search, String instrumentType, String exchange, Integer limit);

    ResponseEntity<WebSocketResponse> getWebsocketData() throws NoSuchAlgorithmException;

    String aliceBlueToken(String userId);

    Result instrumentHistory(AlgoticInstrumentRequest algoticInstrumentRequest);

    ResponseEntity<List<AlgoticResultResponse>> getInstrumentList(
            List<AlgoticInstrumentRequest> algoticInstrumentRequest);
}
