package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.SubscriptionTransactionRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.model.response.PaymentLinkResponse;
import com.algotic.model.response.PaymentStatusResponse;
import com.algotic.model.response.SubscriptionPurchasedResponse;
import com.algotic.model.response.SubscriptionResponse;
import com.algotic.services.PaymentService;
import com.algotic.services.SubscriptionService;
import com.algotic.utils.AlgoticUtils;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/api")
public class SubscriptionController {
    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getSubscription(@RequestParam int limit, int offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("get subscription list", "Get list of all the subscribers", jwtHelper.getUserId(), null));
        return subscriptionService.getSubscription(limit, offset);
    }

    @CrossOrigin
    @GetMapping("/subscription/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionDetails(@PathVariable int id) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "get subscriber details", "Details of the subscriber by its id ", jwtHelper.getUserId(), null));

        return subscriptionService.getSubscriptionDetails(id);
    }

    @CrossOrigin
    @PostMapping("/subscription/transaction")
    public ResponseEntity<GlobalMessageResponse> saveSubscriptionTransaction(
            @Valid @RequestBody SubscriptionTransactionRequest subscriptionTransactionRequest, BindingResult errors) {
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
                .getInfoLog(
                        "Save Subscription Transaction",
                        AlgoticUtils.objectToJsonString(subscriptionTransactionRequest),
                        jwtHelper.getUserId(),
                        null));
        return subscriptionService.saveSubscriptionTransaction(subscriptionTransactionRequest);
    }

    @CrossOrigin
    @GetMapping("/subscription/{id}/transactions")
    public ResponseEntity<List<SubscriptionPurchasedResponse>> getSubscriptionPurchased(
            @PathVariable String id, @RequestParam int limit, int offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Get Subscription Transaction",
                        "details of a particular subscriber will be provided",
                        jwtHelper.getUserId(),
                        null));
        return subscriptionService.getSubscriptionPurchased(id, limit, offset);
    }

    @CrossOrigin
    @PostMapping("/payment/link")
    public ResponseEntity<PaymentLinkResponse> getSubscriptionPurchased() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Payment Gateway", "url for the user to make payment", jwtHelper.getUserId(), null));
        return paymentService.paymentLink();
    }

    @CrossOrigin
    @GetMapping("/payment/status")
    public ResponseEntity<PaymentStatusResponse> paymentStatus() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Payment status", "payment status return", jwtHelper.getUserId(), null));

        return paymentService.paymentStatus();
    }
}
