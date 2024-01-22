package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaytmHoldingResult {

    @JsonProperty("nse_symbol")
    private String nseSymbol;

    @JsonProperty("nse_security_id")
    private String nseSecurityId;

    @JsonProperty("bse_symbol")
    private String bseSymbol;

    @JsonProperty("bse_security_id")
    private String bseSecurityId;

    private String exchange;

    private String quantity;

    @JsonProperty("last_traded_price")
    private String lastTradedPrice;

    @JsonProperty("cost_price")
    private String costPrice;

    @JsonProperty("display_name")
    private String displayName;

    private String segment;

    @JsonProperty("nse_series")
    private String nseSeries;

    @JsonProperty("bse_series")
    private String bseSeries;

    @JsonProperty("isin_code")
    private String isinCode;
}
