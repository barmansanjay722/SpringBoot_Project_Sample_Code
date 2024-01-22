package com.algotic.controllers;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.services.WebhookAliceBlue;
import com.algotic.utils.AlgoticUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping("/api")
public class WebhookController {

    @Autowired
    private WebhookAliceBlue webhookAliceBlue;

    @Autowired
    private LogHandlerConfiguration logConfig;

    @PostMapping("/webhook/instamojo/payment")
    public ResponseEntity saveWebhook(@RequestBody Object webhookRequest) {
        log.info(logConfig
                .getLogHandler()
                .getInfoLog("Save Webhook", AlgoticUtils.objectToJsonString(webhookRequest), null, null));
        return webhookAliceBlue.saveWebhook(webhookRequest);
    }
}
