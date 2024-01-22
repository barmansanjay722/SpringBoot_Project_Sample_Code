package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record TradeBookData(
        @JsonProperty("trade_no") String tradeNumber,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("traded_price") String tradePrice,
        @JsonProperty("exch_trade_time") String tradeTime,
        @JsonProperty("exch_order_time") String orderTime) {}
