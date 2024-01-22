package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentLinkResponse {
    private String instamojoLink;
    private Integer subscriptionTransactionId;
}
