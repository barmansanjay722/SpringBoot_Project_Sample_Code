package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartHistoryResponse {

    @JsonProperty(value = "stat")
    private String stat;

    @JsonProperty(value = "result")
    private List<HistoryValue> historyValue;

    @JsonProperty(value = "message")
    private String message;
}
