package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class PaytmCancelOrderResponse {

    private String status;

    private String message;

    private List<PaytmCancelOrderData> data;

    @JsonProperty("error_code")
    private String errorCode;
}
