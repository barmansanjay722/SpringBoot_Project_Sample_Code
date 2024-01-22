package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InstrumentHistoryRequest {
    @JsonProperty("token")
    private String token;

    @JsonProperty("resolution")
    private String resolution;

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("exchange")
    private String exchange;
}
