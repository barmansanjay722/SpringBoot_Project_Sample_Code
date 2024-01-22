package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderAndTradeBookResponse {
    private String time;
    private String tradeType;
    private String type;
    private String instrument;
    private String tradingSymbol;
    private String product;
    private Integer quantity;
    private String status;
    private String price;
    private String nestOrderNumber;
    private String exchange;
}
