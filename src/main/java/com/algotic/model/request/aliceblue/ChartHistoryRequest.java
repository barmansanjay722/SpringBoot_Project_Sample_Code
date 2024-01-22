package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChartHistoryRequest {

    @JsonProperty(value = "token")
    private String token;

    @JsonProperty(value = "resolution")
    private String resolution;

    @JsonProperty(value = "from")
    private Long from;

    @JsonProperty(value = "to")
    private Long to;

    @JsonProperty(value = "exchange")
    private String exchange;
}
