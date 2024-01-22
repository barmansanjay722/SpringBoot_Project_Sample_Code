package com.algotic.model.response;

import com.algotic.data.entities.TradeExecutions;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeExecutionResponse {
    private Integer total;
    private List<TradeExecutions> result;
}
