package com.algotic.model.response.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class PaytmPositionResponse {

    private String status;

    private String message;

    @JsonProperty("data")
    private List<PaytmPositionData> data;
}
