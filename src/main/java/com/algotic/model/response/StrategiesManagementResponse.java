package com.algotic.model.response;

import com.algotic.data.entities.StrategiesManagements;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StrategiesManagementResponse {
    private int total;
    private List<StrategiesManagements> result;
}
