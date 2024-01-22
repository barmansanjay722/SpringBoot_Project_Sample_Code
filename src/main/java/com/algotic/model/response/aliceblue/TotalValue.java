package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TotalValue {
    @JsonProperty("TotalMCXHoldingValue")
    public String totalMCXHoldingValue;

    @JsonProperty("TotalCSEHoldingValue")
    public String totalCSEHoldingValue;

    @JsonProperty("TotalNSEHoldingValue")
    public String totalNSEHoldingValue;

    @JsonProperty("TotalYSXHoldingValue")
    public String totalYSXHoldingValue;

    @JsonProperty("TotalBSEHoldingValue")
    public String totalBSEHoldingValue;
}
