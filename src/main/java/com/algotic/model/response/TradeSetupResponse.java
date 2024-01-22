package com.algotic.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TradeSetupResponse {
    private Integer total;
    private List<TradesResponse> result;
}
