package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PlaceOrderRequest {
    @JsonProperty("complexty")
    private String complexity;

    @JsonProperty("discqty")
    private Integer discloseQuantity;

    @JsonProperty("exch")
    private String exchange;

    @JsonProperty("pCode")
    private String productCode;

    @JsonProperty("prctyp")
    private String priceType;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("qty")
    private Integer quantity;

    @JsonProperty("ret")
    private String retention;

    @JsonProperty("trading_symbol")
    private String tradingSymbol;

    @JsonProperty("transtype")
    private String transactionType;

    @JsonProperty("trigPrice")
    private Double triggerPrice;

    @JsonProperty("symbol_id")
    private String symbolId;
}
