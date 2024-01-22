package com.algotic.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PortfolioResponse {
    private Double totalLiveInvestment;
    private Double totalPaperInvestment;
    private List<HoldingAlgoticResponse> holdings;
}
