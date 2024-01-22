package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractMasterResponseBody {

    @JsonProperty("exch")
    private String exch;

    @JsonProperty("exchange_segment")
    private String exchange_segment;

    @JsonProperty("formatted_ins_name")
    private String formatted_ins_name;

    @JsonProperty("group_name")
    private String group_name;

    @JsonProperty("instrument_type")
    private String instrument_type;

    @JsonProperty("lot_size")
    private String lot_size;

    @JsonProperty("pdc")
    private String pdc;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("tick_size")
    private String tick_size;

    @JsonProperty("token")
    private String token;

    @JsonProperty("trading_symbol")
    private String trading_symbol;

    @JsonProperty("option_type")
    private String option_type;

    @JsonProperty("strike_price")
    private String strike_price;

    @JsonProperty("expiry_date")
    private BigInteger expiry_date;
}
