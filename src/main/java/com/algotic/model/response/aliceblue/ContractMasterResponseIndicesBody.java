package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContractMasterResponseIndicesBody {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("token")
    private String token;
}
