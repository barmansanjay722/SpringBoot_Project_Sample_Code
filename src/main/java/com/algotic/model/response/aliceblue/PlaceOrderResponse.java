package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceOrderResponse {
    @JsonProperty("stat")
    private String status;

    @JsonProperty("NOrdNo")
    private String nestOrderNumber;

    @JsonProperty("Emsg")
    private String errorMessage;

    @JsonProperty("emsg")
    private String errorMsge;
}
