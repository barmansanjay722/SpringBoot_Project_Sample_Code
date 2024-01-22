package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.AlgoticInstrumentRequest;
import com.algotic.model.response.*;
import com.algotic.model.response.aliceblue.Result;
import com.algotic.services.InstrumentService;
import com.algotic.utils.AlgoticUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class InstrumentController {

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private JwtHelper jwtHelper;

    @CrossOrigin
    @GetMapping("/instruments")
    public ResponseEntity<List<InstrumentSearchAllStockResponse>> getInstruments(
            @RequestParam String search, String instrumentType, String exchange, Integer limit) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Instrument names", "Get all the stocks names by search", jwtHelper.getUserId(), null));
        return instrumentService.getStockNames(search, instrumentType, exchange, limit);
    }

    @CrossOrigin
    @PostMapping("/instruments/history")
    public Result instrumentHistory(
            @Valid @RequestBody AlgoticInstrumentRequest algoticInstrumentRequest, BindingResult errors) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Instrument History ", "Instrument history of a order ", jwtHelper.getUserId(), null));

        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().get(0).getDefaultMessage());
            throw exception;
        }
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Instrument History ",
                        AlgoticUtils.objectToJsonString(algoticInstrumentRequest),
                        jwtHelper.getUserId(),
                        null));
        return instrumentService.instrumentHistory(algoticInstrumentRequest);
    }

    @CrossOrigin
    @PostMapping("/instruments/history/list")
    public Object instrumentHistoryResponse(
            @RequestBody List<AlgoticInstrumentRequest> algoticInstrumentRequest, BindingResult errors) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Instrument History list",
                        AlgoticUtils.objectToJsonString(algoticInstrumentRequest),
                        jwtHelper.getUserId(),
                        null));
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().get(0).getDefaultMessage());
            throw exception;
        }
        return instrumentService.getInstrumentList(algoticInstrumentRequest);
    }
}
