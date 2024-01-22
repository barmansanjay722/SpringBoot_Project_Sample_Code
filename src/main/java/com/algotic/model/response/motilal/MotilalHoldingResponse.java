package com.algotic.model.response.motilal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class MotilalHoldingResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errorcode")
    private String errorCode;

    @JsonProperty("data")
    private List<MotilalHoldingData> data;
}
