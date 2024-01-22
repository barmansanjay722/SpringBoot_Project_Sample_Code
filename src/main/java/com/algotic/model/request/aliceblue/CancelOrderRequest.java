package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CancelOrderRequest {
    @JsonProperty("exch")
    private String exchange;

    @JsonProperty("nestOrderNumber")
    private String nestOrderNumber;

    @JsonProperty("trading_symbol")
    private String tradingSymbol;
}
