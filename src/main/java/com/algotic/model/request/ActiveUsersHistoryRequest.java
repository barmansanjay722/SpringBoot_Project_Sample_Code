package com.algotic.model.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ActiveUsersHistoryRequest {
    @Min(value = 7, message = "Enter days It should be 7 or 30")
    private Integer days;
}
