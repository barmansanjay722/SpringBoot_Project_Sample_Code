package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.config.JwtHelper;
import com.algotic.model.response.*;
import com.algotic.services.StrategyService;
import com.algotic.services.SubscriptionService;
import com.algotic.services.TradeService;
import com.algotic.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class ReportsController {
    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @GetMapping("/reports/renewal")
    public ResponseEntity<SubscriptionRenewalResponse> subscriptionRenewal(Integer limit, Integer offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Get subscription renewal", "details of a particular subscriber for the renewal", null, null));
        return subscriptionService.subscriptionRenewal(limit, offset);
    }

    @CrossOrigin
    @GetMapping("/reports/strategy")
    public ResponseEntity<StrategyReportsResponse> strategyReports(@RequestParam Integer limit, Integer offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog(
                        "Get subscription renewal", "details of a particular subscriber for the renewal", null, null));
        return strategyService.strategyReports(limit, offset);
    }

    @CrossOrigin
    @GetMapping("reports/trades")
    public ResponseEntity<TradeExecutionResponse> trade(@RequestParam Integer limit, Integer offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Trades report", "Report of automated trade", jwtHelper.getUserId(), null));
        return tradeService.tradeExecution(limit, offset);
    }

    @CrossOrigin
    @GetMapping("reports/registration")
    public ResponseEntity<RegistrationResponse> registration(@RequestParam Integer limit, Integer offset) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("customer registration  ", "registration of a customer  ", null, null));
        return userService.registration(limit, offset);
    }
}
