package com.algotic.services;

import com.algotic.model.request.WebhookRequest;
import com.algotic.model.response.GlobalMessageResponse;
import org.springframework.http.ResponseEntity;

public interface WebhookConsumptionService {
    ResponseEntity<GlobalMessageResponse> webhookConsume(WebhookRequest webhookRequest, String webhookUrl);
}
