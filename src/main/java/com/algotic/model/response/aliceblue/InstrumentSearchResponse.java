package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentSearchResponse {

    @JsonProperty("exch")
    private String exchange;

    @JsonProperty("exchange_segment")
    private String exchangeSegment;

    @JsonProperty("symbol")
    private String tradingSymbol;

    @JsonProperty("token")
    private String token;

    @JsonProperty("formattedInsName")
    private String formattedInsName;

    @JsonProperty("lotSize")
    private String lotSize;

    @JsonProperty("ticSize")
    private String ticSize;

    @JsonProperty("expiry")
    private Date expiry;
}
