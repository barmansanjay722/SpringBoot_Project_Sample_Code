package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.ResendOtpRequest;
import com.algotic.model.request.VerifyRequest;
import com.algotic.model.response.OtpResponse;
import com.algotic.model.response.VerifyResponse;
import com.algotic.services.OtpService;
import com.algotic.services.VerifyService;
import com.algotic.utils.AlgoticUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class OtpController {

    @Autowired
    public VerifyService verifyService;

    @Autowired
    OtpService otpService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @PostMapping(
            value = "/otp/verify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VerifyResponse> verifyOtp(
            @Valid @RequestBody VerifyRequest verifyRequest, BindingResult errors) {
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
                .getInfoLog("verify otp", AlgoticUtils.objectToJsonString(verifyRequest), null, null));
        return verifyService.verifyOtpAndGetToken(verifyRequest);
    }

    @CrossOrigin
    @PostMapping("/otp/resend")
    public ResponseEntity<OtpResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest resendOtpRequest, BindingResult errors) {
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
                .getInfoLog("resend Otp", AlgoticUtils.objectToJsonString(resendOtpRequest), null, null));
        return otpService.resendOtp(resendOtpRequest);
    }
}
