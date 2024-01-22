package com.algotic.model.response.motilal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MotilalLTPData {

    private String exchange;

    @JsonProperty("scripcode")
    private String scripCode;

    private String open;

    private String high;

    private String low;

    private String close;

    private String ltp;

    private String volume;
}
