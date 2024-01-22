package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaytmLTPData {

    private String tradable;

    private String mode;

    @JsonProperty("security_id")
    private String securityId;

    @JsonProperty("last_price")
    private String lastTradedPrice;

    private boolean found;
}
