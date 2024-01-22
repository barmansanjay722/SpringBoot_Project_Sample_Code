package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private Integer subscribers;
    private Integer revenue;
    private Integer activeUsersToday;
    private Integer inactiveUsersToday;
}
