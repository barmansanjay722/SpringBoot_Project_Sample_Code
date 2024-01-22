package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InstrumentResponse {
    @JsonProperty("exch")
    private String exchange;

    @JsonProperty("exchange_segment")
    private String exchangeSegment;

    @JsonProperty("formatted_ins_name")
    private String formattedInsName;

    @JsonProperty("instrument_type")
    private String instrumentType;

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("lot_size")
    private String lotSize;

    @JsonProperty("pdc")
    private String pdc;

    @JsonProperty("option_type")
    private String optionType;

    @JsonProperty("strike_price")
    private String strikePrice;

    @JsonProperty("expire_date")
    private String expiryDate;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("tick_size")
    private String tickSize;

    @JsonProperty("token")
    private String token;

    @JsonProperty("trading_symbol")
    private String tradingSymbol;
}
