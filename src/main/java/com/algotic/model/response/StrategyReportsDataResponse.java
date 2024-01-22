package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyReportsDataResponse {
    private Integer strategyId;
    private String strategyName;
    private Integer usageOfStrategy;
    private Integer noOfUser;
}
