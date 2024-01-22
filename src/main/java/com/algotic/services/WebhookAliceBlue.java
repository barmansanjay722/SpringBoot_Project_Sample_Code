package com.algotic.services;

import org.springframework.http.ResponseEntity;

public interface WebhookAliceBlue {

    ResponseEntity saveWebhook(Object webhookRequest);
}
