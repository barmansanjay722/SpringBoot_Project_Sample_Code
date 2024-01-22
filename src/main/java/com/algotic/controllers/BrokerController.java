package com.algotic.controllers;

import com.algotic.base.BrokerMgmtServiceFactory;
import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.constants.BrokerEnum;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.aliceblue.AliceBlueSessionRequest;
import com.algotic.model.request.motilal.MotilalSessionRequest;
import com.algotic.model.request.paytm.PaytmSessionRequest;
import com.algotic.model.response.BrokerResponse;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.services.impl.CommonBrokerMgmtServiceImpl;
import com.algotic.utils.AlgoticUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api")
@CrossOrigin
public class BrokerController {

    @Autowired
    private CommonBrokerMgmtServiceImpl brokerService;

    @Autowired
    private BrokerMgmtServiceFactory brokerServiceFactory;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @PostMapping("/broker/customer")
    public ResponseEntity<GlobalMessageResponse> saveCustomerBrokerDetails(
            @Valid @RequestBody AliceBlueSessionRequest customerBrokerRequest, BindingResult errors) {
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
                        "Start -> Save broker customer Details",
                        AlgoticUtils.objectToJsonString(customerBrokerRequest),
                        jwtHelper.getUserId(),
                        null));
        return brokerServiceFactory.getService(BrokerEnum.ALICE_BLUE).saveCustomerBrokerDetails(customerBrokerRequest);
    }

    @CrossOrigin
    @PostMapping("/broker/motilal/customer")
    public ResponseEntity<GlobalMessageResponse> saveMotilalCustomerBrokerDetails(
            @Valid @RequestBody MotilalSessionRequest motilalBrokerReq, BindingResult errors) {
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
                        "Start -> Save broker customer Details",
                        AlgoticUtils.objectToJsonString(motilalBrokerReq),
                        jwtHelper.getUserId(),
                        null));
        return brokerServiceFactory.getService(BrokerEnum.MOTILAL_OSWAL).saveCustomerBrokerDetails(motilalBrokerReq);
    }

    @PostMapping("/broker/paytm/customer")
    public ResponseEntity<GlobalMessageResponse> savePaytmCustomerBrokerDetails(
            @Valid @RequestBody PaytmSessionRequest paytmBrokerReq, BindingResult errors) {
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
                        "Start -> Save broker customer Details",
                        AlgoticUtils.objectToJsonString(paytmBrokerReq),
                        jwtHelper.getUserId(),
                        null));
        return brokerServiceFactory.getService(BrokerEnum.PAYTM_MONEY).saveCustomerBrokerDetails(paytmBrokerReq);
    }

    @CrossOrigin
    @GetMapping("/brokers")
    public ResponseEntity<List<BrokerResponse>> getBrokers(int limit, int offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Broker details start",
                        "Process for get the details of all brokers",
                        jwtHelper.getUserId(),
                        null));
        return brokerService.getBrokers(limit, offset);
    }
}
