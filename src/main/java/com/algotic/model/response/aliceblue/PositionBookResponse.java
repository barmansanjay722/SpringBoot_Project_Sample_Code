package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionBookResponse {

    @JsonProperty("realisedprofitloss")
    public String realisedProfitLoss;

    @JsonProperty("Fillsellamt")
    public String fillSellAmt;

    @JsonProperty("Netqty")
    public String netQty;

    @JsonProperty("Symbol")
    public String symbol;

    @JsonProperty("Instname")
    public String instName;

    @JsonProperty("Expdate")
    public String expDate;

    @JsonProperty("BLQty")
    public String bLQty;

    @JsonProperty("Opttype")
    public String optType;

    @JsonProperty("LTP")
    public String lTP;

    @JsonProperty("Token")
    public String token;

    @JsonProperty("Fillbuyamt")
    public String fillBuyAmt;

    @JsonProperty("Fillsellqty")
    public String fillSellQty;

    @JsonProperty("Tsym")
    public String tsym;

    @JsonProperty("sSqrflg")
    public String sSqrflg;

    @JsonProperty("unrealisedprofitloss")
    public String unrealisedProfitLoss;

    @JsonProperty("Buyavgprc")
    public String buyAvgPrc;

    @JsonProperty("NetBuyavgprc")
    public String netBuyAvgPrc;

    @JsonProperty("MtoM")
    public String mtoM;

    @JsonProperty("stat")
    public String stat;

    @JsonProperty("Sqty")
    public String sqty;

    @JsonProperty("s_NetQtyPosConv")
    public String sNetQtyPosConv;

    @JsonProperty("Sellavgprc")
    public String sellAvgPrc;

    @JsonProperty("NetSellavgprc")
    public String netSellAvgPrc;

    @JsonProperty("PriceDenomenator")
    public String priceDenominator;

    @JsonProperty("PriceNumerator")
    public String priceNumerator;

    @JsonProperty("actid")
    public String actId;

    @JsonProperty("posflag")
    public Boolean posFlag;

    @JsonProperty("Bqty")
    public String bqty;

    @JsonProperty("Stikeprc")
    public String stikePrc;

    @JsonProperty("Pcode")
    public String pCode;

    @JsonProperty("BEP")
    public String bEP;

    @JsonProperty("Exchange")
    public String exchange;

    @JsonProperty("GeneralDenomenator")
    public String generalDenominator;

    @JsonProperty("Series")
    public String series;

    @JsonProperty("Type")
    public String type;

    @JsonProperty("Netamt")
    public String netAmt;

    @JsonProperty("companyname")
    public String companyName;

    @JsonProperty("Fillbuyqty")
    public String fillBuyQty;

    @JsonProperty("GeneralNumerator")
    public String generalNumerator;

    @JsonProperty("Exchangeseg")
    public String exchangeSeg;

    @JsonProperty("discQty")
    public String discQty;

    @JsonProperty("netbuyqty")
    public String netbuyqty;

    @JsonProperty("netsellqty")
    public String netsellqty;
}
