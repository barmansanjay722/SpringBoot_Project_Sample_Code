package com.algotic.model.response;

import com.algotic.data.entities.CustomerManagements;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerManagementResponse {
    private int total;
    private List<CustomerManagements> result;
}
