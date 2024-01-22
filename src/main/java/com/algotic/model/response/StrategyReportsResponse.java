package com.algotic.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StrategyReportsResponse {
    private Integer total;
    private List<StrategyReportsDataResponse> result;
}
