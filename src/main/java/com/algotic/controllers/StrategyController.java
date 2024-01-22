package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.StrategyRequest;
import com.algotic.model.response.*;
import com.algotic.services.StrategyService;
import com.algotic.utils.AlgoticUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class StrategyController {

    @Autowired
    StrategyService strategyService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @CrossOrigin
    @GetMapping("/strategy/all")
    public ResponseEntity<List<StrategiesResponse>> getStrategiesNames() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "get strategy names", "Get all the name available in the table", jwtHelper.getUserId(), null));
        return strategyService.getStrategiesNames();
    }

    @CrossOrigin
    @GetMapping("/strategies")
    public ResponseEntity<StrategiesManagementResponse> getStrategies(
            @Nullable @RequestParam String name,
            @Nullable @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @Nullable @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @Nullable @RequestParam String status,
            @Nullable @RequestParam Integer limit,
            @Nullable @RequestParam Integer offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("get all the strategy", "Get all the Strategy Data ", jwtHelper.getUserId(), null));
        return strategyService.getStrategies(name, from, to, status, limit, offset);
    }

    @CrossOrigin
    @PostMapping("/strategy")
    public ResponseEntity<StrategyDetailsResponse> saveStrategy(
            @Valid @RequestBody StrategyRequest strategyRequest, BindingResult errors, boolean isActive) {
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().stream()
                            .sorted((error1, error2) ->
                                    error1.getDefaultMessage().compareTo(error2.getDefaultMessage()))
                            .toList()
                            .get(0)
                            .getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("save the strategy", AlgoticUtils.objectToJsonString(strategyRequest), null, null));
        return strategyService.saveStrategy(strategyRequest);
    }

    @CrossOrigin
    @GetMapping("/strategy/{id}")
    public ResponseEntity<StrategyDetailsResponse> getStrategyDetails(@PathVariable int id) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "get strategy details",
                        "Get all the strategy details of a particular strategy through its id",
                        jwtHelper.getUserId(),
                        null));
        return strategyService.getStrategyDetails(id);
    }

    @CrossOrigin
    @PostMapping("/strategy/{id}/{type}")
    public ResponseEntity<GlobalMessageResponse> activeInactiveStrategy(
            @PathVariable int id, @PathVariable String type) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Active/Inactive ", "Active/Inactive By id ", jwtHelper.getUserId(), null));
        return strategyService.activeInactiveStrategy(id, type);
    }

    @CrossOrigin
    @DeleteMapping("/strategy/{id}")
    public ResponseEntity<GlobalMessageResponse> deleteStrategy(@PathVariable int id) {
        log.info(logConfig.getLogHandler().getInfoLog("Delete Strategy  ", "Delete strategy by id ", null, null));
        return strategyService.deleteStrategy(id);
    }
}
