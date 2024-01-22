package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ModifyOrderRequest {
    @JsonProperty("transtype")
    private String transactionType;

    @JsonProperty("discqty")
    private Integer disclosedQuantity;

    @JsonProperty("exch")
    private String exchange;

    @JsonProperty("trading_symbol")
    private String tradingSymbol;

    @JsonProperty("nestOrderNumber")
    private String nestOrderNumber;

    @JsonProperty("prctyp")
    private String priceType;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("qty")
    private Integer quantity;

    @JsonProperty("pCode")
    private String productCode;

    @JsonProperty("trigPrice")
    private Double triggerPrice;

    @JsonProperty("filledQuantity")
    private Integer filledQuantity;
}
