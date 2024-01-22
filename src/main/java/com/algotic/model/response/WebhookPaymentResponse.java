package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPaymentResponse {

    private PaymentResponse payment;
    private BuyerResponse buyer;
    private String message_authentication_code;
}
