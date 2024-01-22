package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HoldingResponse {

    @JsonProperty("HoldingVal")
    private List<HoldingValue> holdingValue;

    @JsonProperty("Totalval")
    private TotalValue totalValue;
}
