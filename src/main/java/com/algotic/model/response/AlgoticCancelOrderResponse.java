package com.algotic.model.response;

import com.algotic.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlgoticCancelOrderResponse {
    private String status;
    private String nestOrderNumber;

    @JsonIgnoreProperties
    private boolean isException;

    @JsonIgnoreProperties
    private ErrorCode errorode;
}
