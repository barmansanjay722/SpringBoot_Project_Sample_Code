package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlgoticSquareOffRequest {
    @NotNull(message = "Token is mandatory")
    private Integer token;

    @NotBlank(message = "Product code is mandatory")
    private String productCode;

    private Double price;

    private String tradeType;
}
