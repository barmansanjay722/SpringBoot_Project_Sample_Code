package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderHistoryResponse {
    @JsonProperty("stat")
    private String stat;

    @JsonProperty("Trsym")
    private String tradingSymbol;

    @JsonProperty("Prc")
    private Integer price;

    @JsonProperty("Qty")
    private Integer quantity;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Action")
    private String action;

    @JsonProperty("Ordtype")
    private String orderType;

    @JsonProperty("PriceNumerator")
    private String priceNumerator;

    @JsonProperty("GeneralNumerator")
    private String generalNumerator;

    @JsonProperty("PriceDenomenator")
    private String priceDenominator;

    @JsonProperty("GeneralDenomenator")
    private String generalDenominator;

    @JsonProperty("bqty")
    private String boardLot;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("nestordernumber")
    private String nestOrderNumber;

    @JsonProperty("nestreqid")
    private String nestRequestId;

    @JsonProperty("symbolname")
    private String symbolName;

    @JsonProperty("averageprice")
    private Integer averagePrice;

    @JsonProperty("triggerprice")
    private Integer triggerPrice;

    @JsonProperty("disclosedqty")
    private String disclosedQuantity;

    @JsonProperty("exchangeorderid")
    private String exchangeOrderId;

    @JsonProperty("rejectionreason")
    private String rejectionReason;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("productcode")
    private String productCode;

    @JsonProperty("reporttype")
    private String reportType;

    @JsonProperty("customerfirm")
    private String customerFirm;

    @JsonProperty("exchangetimestamp")
    private Integer exchangeTimeStamp;

    @JsonProperty("ordersource")
    private String orderSource;

    @JsonProperty("filldateandtime")
    private String fillDateandTime;

    @JsonProperty("ordergenerationtype")
    private String orderGenerationType;

    @JsonProperty("scripname")
    private String scripName;

    @JsonProperty("legorderindicator")
    private String legOrderIndicator;

    @JsonProperty("filledShares")
    private String filledShares;
}
