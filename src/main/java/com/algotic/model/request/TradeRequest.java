package com.algotic.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TradeRequest {

    @NotBlank(message = "Enter Trade type it is mandatory")
    @Pattern(
            regexp = "^([a-zA-Z])+$",
            message = "Enter trade type with alphabets it doesn't allow special characters,numbers and spaces")
    private String tradeType;

    @NotBlank(message = "Instrument it is mandatory")
    private String instrumentName;

    @NotBlank(message = "Trading symbol it is mandatory")
    private String tradingSymbol;

    @NotBlank(message = "Enter exchange it is mandatory")
    @Pattern(
            regexp = "^([a-zA-Z])+$",
            message = "Enter Exchange with alphabets it doesn't allow special characters,numbers and spaces")
    private String exchange;

    private Integer strategyId;

    @Min(value = 0, message = "Enter valid lot Size.It should be greater than 0")
    private Integer lotSize;

    @Min(value = 0, message = "Enter valid Stop Loss Price.It should be greater than 0")
    private Double stopLossPrice;

    @Min(value = 0, message = "Enter valid Target Profit.It should be greater than 0")
    private Double targetProfit;

    @NotBlank(message = "Enter order type it is mandatory")
    @Pattern(
            regexp = "^([a-zA-Z])+$",
            message = "Enter order type with alphabets it doesn't allow special characters,numbers and spaces")
    private String orderType;

    @NotBlank(message = "Instrument token is mandatory")
    private String token;

    @NotBlank(message = "stock type must required")
    private String stockType;
}
