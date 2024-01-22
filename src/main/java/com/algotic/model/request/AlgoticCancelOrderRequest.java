package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlgoticCancelOrderRequest {
    @NotBlank
    private String exchange;

    private String nestOrderNumber;

    @NotBlank
    private String tradingSymbol;
}
