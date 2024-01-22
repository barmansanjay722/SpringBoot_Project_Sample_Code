package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.model.request.WebhookRequest;
import com.algotic.model.response.GlobalMessageResponse;
import com.algotic.services.WebhookConsumptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class WebhookConsumptionController {

    @Autowired
    private WebhookConsumptionService webhookConsumptionService;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @CrossOrigin
    @PostMapping("/tradingview/webhooks/{webhookUrlData}")
    public ResponseEntity<GlobalMessageResponse> webhookConsumption(
            @RequestBody WebhookRequest webhookRequest, @PathVariable String webhookUrlData) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("webhook consumption", "start-> webhook consumption ", null, null));
        return webhookConsumptionService.webhookConsume(webhookRequest, webhookUrlData);
    }
}
