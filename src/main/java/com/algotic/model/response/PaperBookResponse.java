package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaperBookResponse {
    private String tradingSymbol;
    private Double price;
    private String productCode;
    private String transactionType;
    private String userId;
    private String exchange;
    private Integer quantity;
    private String orderType;
    private Double triggerPrice;
    private String tradeType;
    private String priceType;
}
