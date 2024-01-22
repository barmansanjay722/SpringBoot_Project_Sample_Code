package com.algotic.services;

import com.algotic.model.request.StrategyRequest;
import com.algotic.model.response.*;
import java.util.Date;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface StrategyService {
    ResponseEntity<List<StrategiesResponse>> getStrategiesNames();

    ResponseEntity<StrategiesManagementResponse> getStrategies(
            String name, Date from, Date to, String status, Integer limit, Integer offset);

    ResponseEntity<StrategyDetailsResponse> saveStrategy(StrategyRequest strategyRequest);

    ResponseEntity<StrategyDetailsResponse> getStrategyDetails(int id);

    ResponseEntity<GlobalMessageResponse> activeInactiveStrategy(int id, String type);

    ResponseEntity<GlobalMessageResponse> deleteStrategy(int id);

    ResponseEntity<StrategyReportsResponse> strategyReports(Integer limit, Integer offset);
}
