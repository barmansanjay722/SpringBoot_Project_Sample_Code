package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlaceOrderData(
        @JsonProperty("order_no") String uniqueOrderId, @JsonProperty("oms_error_code") String errorCode) {}
