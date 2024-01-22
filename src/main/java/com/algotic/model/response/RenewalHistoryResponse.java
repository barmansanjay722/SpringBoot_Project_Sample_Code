package com.algotic.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenewalHistoryResponse {
    private List<RenewalResponse> result;
    private String startDate;
    private String endDate;
    private String broker;
    private Long noOfDaysLeft;
}
