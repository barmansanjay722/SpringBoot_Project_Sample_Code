package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private String payment_id;
    private String status;
    private Double amount;
    private String currency;
    private String completed_at;
    private String resource_uri;
}
