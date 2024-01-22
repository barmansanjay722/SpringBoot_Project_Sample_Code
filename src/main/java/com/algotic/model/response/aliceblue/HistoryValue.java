package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryValue {

    @JsonProperty(value = "volume")
    private Double volume;

    @JsonProperty(value = "high")
    private Double high;

    @JsonProperty(value = "low")
    private Double low;

    @JsonProperty(value = "time")
    private String time;

    @JsonProperty(value = "close")
    private Double close;

    @JsonProperty(value = "open")
    private Double open;
}
