package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.request.AlgoticCancelOrderRequest;
import com.algotic.model.request.AlgoticModifyRequest;
import com.algotic.model.request.AlgoticSquareOffRequest;
import com.algotic.model.request.OrderRequest;
import com.algotic.model.response.*;
import com.algotic.services.OrderService;
import com.algotic.utils.AlgoticUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.security.NoSuchAlgorithmException;
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
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @Autowired
    private JwtHelper jwtHelper;

    @CrossOrigin
    @PostMapping("/order/place")
    public ResponseEntity<GlobalMessageResponse> placeOrder(
            @Valid @RequestBody OrderRequest orderRequest, BindingResult errors) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Place Order", AlgoticUtils.objectToJsonString(orderRequest), null, null));
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
        return orderService.placeOrder(orderRequest);
    }

    @CrossOrigin
    @GetMapping("/order/positions")
    public ResponseEntity<List<BookPositionResponse>> positionBook() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Order position book", "get position of orders", jwtHelper.getUserId(), null));

        return orderService.getPositions();
    }

    @CrossOrigin
    @GetMapping("/order/holdings")
    public ResponseEntity<List<HoldingAlgoticResponse>> holding() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Order Holdings",
                        AlgoticUtils.objectToJsonString("All orders Which Are in Holdings"),
                        jwtHelper.getUserId(),
                        "200"));
        return orderService.getHoldings();
    }

    @CrossOrigin
    @GetMapping("/order/book")
    public ResponseEntity<List<OrderAndTradeBookResponse>> orderBook(@Nullable String type) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Order book", AlgoticUtils.objectToJsonString("All orders"), jwtHelper.getUserId(), "200"));
        return orderService.orderBook(type);
    }

    @CrossOrigin
    @PostMapping("/order/cancel")
    public ResponseEntity<AlgoticCancelOrderResponse> cancelOrder(
            @Valid @RequestBody AlgoticCancelOrderRequest algoticRequest, BindingResult errors) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Cancel order", AlgoticUtils.objectToJsonString(algoticRequest), jwtHelper.getUserId(), null));
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().get(0).getDefaultMessage());
            throw exception;
        }
        return orderService.cancelOrder(algoticRequest);
    }

    @CrossOrigin
    @PostMapping("/order/squareOffAll")
    public ResponseEntity<AlgoticSquareOffResponse> squareOffAll() throws NoSuchAlgorithmException {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("squareOff", AlgoticUtils.objectToJsonString("Square off all the orders"), null, null));
        return orderService.squareOffAll();
    }

    @CrossOrigin
    @GetMapping("/order/portfolio")
    public ResponseEntity<PortfolioResponse> getTotalInvestmentValue() {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "customer get total investment",
                        AlgoticUtils.objectToJsonString("Total investment of the customer"),
                        jwtHelper.getUserId(),
                        null));
        return orderService.getPortfolioData();
    }

    @CrossOrigin
    @PostMapping("/order/modify")
    public ResponseEntity<GlobalMessageResponse> orderModify(
            @Valid @RequestBody AlgoticModifyRequest modifyRequest, BindingResult errors) {
        log.info(logConfig.getLogHandler().getInfoLog("order modify", "modify Order", null, null));
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().get(0).getDefaultMessage());
            throw exception;
        }
        return orderService.modifyOrder(modifyRequest);
    }

    @CrossOrigin
    @PostMapping("/order/squareOff")
    public ResponseEntity<AlgoticSquareOffResponse> squareOff(
            @Valid @RequestBody AlgoticSquareOffRequest algoticSquareOffRequest, BindingResult errors) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "square off",
                        AlgoticUtils.objectToJsonString(algoticSquareOffRequest),
                        jwtHelper.getUserId(),
                        null));
        if (errors.hasErrors()) {
            AlgoticException exception = new AlgoticException(CommonErrorCode.BAD_REQUEST);
            exception
                    .getErrorCode()
                    .setErrorMessage(errors.getAllErrors().get(0).getDefaultMessage());
            throw exception;
        }
        return orderService.squareOff(algoticSquareOffRequest);
    }
}
