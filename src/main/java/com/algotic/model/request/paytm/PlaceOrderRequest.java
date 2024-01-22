package com.algotic.model.request.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PlaceOrderRequest(
        @JsonProperty("txn_type") String transactionType,
        @JsonProperty("exchange") String exchange,
        String segment,
        @JsonProperty("product") String productCode,
        @JsonProperty("security_id") String scripCode,
        String quantity,
        @JsonProperty("validity") String retension,
        @JsonProperty("order_type") String orderType,
        String price,
        @JsonProperty("trigger_price") String triggerPrice,
        @JsonProperty("off_mkt_flag") String amoOrder,
        String source) {}
