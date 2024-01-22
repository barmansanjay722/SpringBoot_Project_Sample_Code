package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TradeWebhookResponse {

    private String strategyScript;
    private String buyAlertMessage;
    private String sellAlertMessage;
    private String webhookURL;
}
