package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.data.repositories.TradesRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.TradeRequest;
import com.algotic.model.response.*;
import com.algotic.services.TradeService;
import com.algotic.utils.AlgoticUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private TradesRepo tradesRepo;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    JwtHelper jwtHelper;

    @CrossOrigin
    @GetMapping("/trades")
    public ResponseEntity<TradeSetupResponse> getTrades(int limit, int offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Get All Trades", "All the trades will be shown here ", jwtHelper.getUserId(), null));
        return tradeService.getTrades(limit, offset);
    }

    @CrossOrigin
    @PostMapping("/trade")
    public ResponseEntity<TradeWebhookResponse> saveTrade(
            @Valid @RequestBody TradeRequest tradeRequest, BindingResult errors) {

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
                .getInfoLog("Save Trade", AlgoticUtils.objectToJsonString(tradeRequest), jwtHelper.getUserId(), null));
        return tradeService.saveTrade(tradeRequest);
    }

    @CrossOrigin
    @PostMapping("/trade/{tradeId}/{type}")
    public ResponseEntity<GlobalMessageResponse> deleteTrade(@PathVariable String tradeId, @PathVariable String type) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("delete trade", "Delete the trade by the particular id", jwtHelper.getUserId(), null));
        return tradeService.tradeActiveInactive(tradeId, type);
    }

    @CrossOrigin
    @DeleteMapping("/trade/{id}")
    public ResponseEntity<GlobalMessageResponse> deleteUser(@PathVariable String id) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Delete trade  ", "Delete trade by id ", jwtHelper.getUserId(), null));
        return tradeService.deleteTrade(id);
    }
}
