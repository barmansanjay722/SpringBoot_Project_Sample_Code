package com.algotic.model.request.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SquareOffRequest {
    @JsonProperty(value = "scripToken")
    private Integer token;

    @JsonProperty(value = "pCode")
    private String productCode;
}
