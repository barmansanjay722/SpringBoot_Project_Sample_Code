package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenewalResponse {
    private String date;
    private Double amount;
    private String invoice;
    private Integer subscriptionTransactionId;
}
