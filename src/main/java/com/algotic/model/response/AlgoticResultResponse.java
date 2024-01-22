package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlgoticResultResponse {
    private Double volume;

    private Double high;

    private Double low;

    private String time;

    private Double open;

    private Double close;
}
