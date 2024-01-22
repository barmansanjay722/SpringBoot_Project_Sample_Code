package com.algotic.model.response.motilal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MotilalHoldingData {

    @JsonProperty("clientcode")
    private String clientCode;

    @JsonProperty("scripisinno")
    private String scripisinno;

    @JsonProperty("dpquantity")
    private String totalQuantity;

    @JsonProperty("blockedquantity")
    private String blockedQuantity;

    @JsonProperty("scripname")
    private String scripName;

    @JsonProperty("buyavgprice")
    private String buyAvgPrice;

    @JsonProperty("poaquantity")
    private String poaQuantity;

    @JsonProperty("collateralquantity")
    private String collateralQuantity;

    @JsonProperty("outstandingquantity")
    private String outstandingQuantity;

    @JsonProperty("debitstockquantity")
    private String debitStockQuantity;

    @JsonProperty("nonpoaquantity")
    private String nonPoaQuantity;

    @JsonProperty("rmssellingquantity")
    private String rmsSellingQuantity;

    @JsonProperty("btstquantity")
    private String btstQuantity;

    @JsonProperty("buybackquantity")
    private String buyBackQuantity;

    @JsonProperty("tpinquantity")
    private String tpinQuantity;

    @JsonProperty("slbmquantity")
    private String slbmQuantity;

    @JsonProperty("nbfcquantity")
    private String nbfcQuantity;

    @JsonProperty("bsesymboltoken")
    private String bseScripCode;

    @JsonProperty("nsesymboltoken")
    private String nseSymbolToken;
}
