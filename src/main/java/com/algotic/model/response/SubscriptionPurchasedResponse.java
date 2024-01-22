package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubscriptionPurchasedResponse {
    private Integer id;
    private String transactionId;
    private String userId;
    private String status;
    private Double amount;
}
