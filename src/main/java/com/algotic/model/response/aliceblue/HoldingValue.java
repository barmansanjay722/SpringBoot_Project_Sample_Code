package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HoldingValue {
    @JsonProperty("Holdqty")
    private String holdQuantity;

    @JsonProperty("Nsetsym")
    private String nseTradingSymbol;

    @JsonProperty("Bsetsym")
    private String bseTradingSymbol;

    @JsonProperty("Ltp")
    private String lastTradePrice;

    @JsonProperty("SellableQty")
    public String sellableQty;

    @JsonProperty("Pcode")
    public String productCode;

    @JsonProperty("ExchSeg1")
    private String exchangeSegmentOne;

    @JsonProperty("ExchSeg2")
    private String exchangeSegmentTwo;

    @JsonProperty("Token1")
    private String tokenOne;

    @JsonProperty("Token2")
    private String tokenTwo;

    @JsonProperty("Haircut")
    private String hairCut;

    @JsonProperty("Price")
    private String price;

    @JsonProperty("pdc")
    private String pdc;
}
