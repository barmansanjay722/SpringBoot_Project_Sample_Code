package com.algotic.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlgoticModifyRequest {
    private String transactionType;
    private Integer disclosedQuantity;
    private String exchange;
    private String tradingSymbol;

    @NotNull
    private String nestOrderNumber;

    @NotBlank(message = "Price type is mandatory")
    private String priceType;

    private Double price;

    @Min(value = 0, message = "Enter Quantity it should be greater than 0")
    private Integer quantity;

    @NotBlank(message = "Product code is mandatory")
    private String productCode;

    private Double triggerPrice;
    private Integer filledQuantity;
}
