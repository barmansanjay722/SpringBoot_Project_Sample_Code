package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InstrumentHistoryResponse {
    @JsonProperty("stat")
    private String status;

    @JsonProperty("result")
    private List<Result> result;
}
