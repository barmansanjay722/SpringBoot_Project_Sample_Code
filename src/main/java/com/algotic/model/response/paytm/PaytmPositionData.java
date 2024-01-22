package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaytmPositionData {

    @JsonProperty("client_id")
    private String clientId;

    private String exchange;

    private String segment;

    private String product;

    @JsonProperty("security_id")
    private String securityId;

    @JsonProperty("mkt_type")
    private String marketType;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("tot_buy_qty")
    private String totalBuyQuantity;

    @JsonProperty("tot_buy_val")
    private String totalBuyValue;

    @JsonProperty("tot_sell_qty")
    private String totalSellQuantity;

    @JsonProperty("tot_sell_val")
    private String totalSellValue;

    @JsonProperty("net_qty")
    private String netQuantity;

    @JsonProperty("net_val")
    private String netValue;

    @JsonProperty("last_traded_price")
    private String lastTradedPrice;

    @JsonProperty("realised_profit")
    private String profitAndLoss;

    @JsonProperty("display_product")
    private String productType;

    @JsonProperty("lot_size")
    private String lotSize;

    @JsonProperty("expiry_date")
    private String expiryDate;

    @JsonProperty("buy_avg")
    private String totalBuyAverage;

    @JsonProperty("sell_avg")
    private String totalSellAverage;
}
