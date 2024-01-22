package com.algotic.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderRequest {

    private String complexity;

    private Integer discloseQuantity;

    @NotBlank(message = "Enter Product Code it is mandatory")
    private String productCode;

    private String priceType;

    @Min(value = 0, message = "Enter Price it  should be greater than 0")
    private Double price;

    @Min(value = 0, message = "Enter Quantity it should be greater than 0")
    private Integer quantity;

    @NotBlank(message = "Enter Trading Symbol it is mandatory")
    private String tradingSymbol;

    @NotBlank(message = "Enter Instrument name it is mandatory")
    private String instrumentName;

    @NotBlank(message = "Enter  Transaction type it is mandatory")
    private String transactionType;

    @Min(value = 0, message = "Enter Trigger price it should be greater than 0")
    private Double triggerPrice;

    private String exchange;

    private String token;

    @NotBlank(message = "Trade type is mandatory")
    private String tradeType;

    private Boolean isHolding;
    private Date expiry;
}
