package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaytmCancelOrderData {

    @JsonProperty("oms_error_code")
    private String omsErrorCode;

    @JsonProperty("order_no")
    private String orderNumber;
}
