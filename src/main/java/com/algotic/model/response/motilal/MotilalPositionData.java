package com.algotic.model.response.motilal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MotilalPositionData {

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("clientcode")
    private String clientCode;

    @JsonProperty("productname")
    private String productName;

    @JsonProperty("symboltoken")
    private String symbolToken;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("buyquantity")
    private String buyQuantity;

    @JsonProperty("buyamount")
    private String buyAmount;

    @JsonProperty("sellquantity")
    private String sellQuantity;

    @JsonProperty("sellamount")
    private String sellAmount;

    @JsonProperty("daybuyquantity")
    private String dayBuyQuantity;

    @JsonProperty("daybuyamount")
    private String dayBuyAmount;

    @JsonProperty("daysellquantity")
    private String daySellQuantity;

    @JsonProperty("daysellamount")
    private String daySellAmount;

    @JsonProperty("LTP")
    private String ltp;

    @JsonProperty("marktomarket")
    private String markToMarket;

    @JsonProperty("bookedprofitloss")
    private String bookedProfitLoss;

    @JsonProperty("cfbuyquantity")
    private String cfBuyQuantity;

    @JsonProperty("cfbuyamount")
    private String cfBuyAmount;

    @JsonProperty("cfsellquantity")
    private String cfSellQuantity;

    @JsonProperty("cfsellamount")
    private String cfSellAmount;

    @JsonProperty("actualbookedprofitloss")
    private String actualBookedProfitLoss;

    @JsonProperty("actualMarkToMarket")
    private String actualmarktomarket;

    @JsonProperty("series")
    private String series;

    @JsonProperty("expiryDate")
    private String expirydate;

    @JsonProperty("strikeprice")
    private String strikePrice;

    @JsonProperty("optiontype")
    private String optionType;
}
