package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderHistoryRequest {
    @JsonProperty("nestOrderNumber")
    private Integer nestOrderNumber;
}
