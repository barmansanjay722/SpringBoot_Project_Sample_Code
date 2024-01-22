package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModifyOrderResponse {
    @JsonProperty("stat")
    private String status;

    @JsonProperty("Result")
    private Integer result;
}
