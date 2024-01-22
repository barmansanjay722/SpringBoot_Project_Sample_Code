package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InstrumentSearchRequest {

    @JsonProperty("symbol")
    private String search;

    @JsonProperty("exchange")
    private String[] exchange;
}
