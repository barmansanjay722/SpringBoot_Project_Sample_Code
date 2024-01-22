package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BookPositionResponse {

    private String tradingSymbol;
    private String instrumentName;
    private String tradeType;
    private String orderType;
    private String quantity;
    private String buyAverage;
    private String sellAverage;
    private String lastTradePrice;
    private String profitAndLoss;
    private String exchange;
    private String token;
    private Boolean isStock;
    private String netQuantity;
    private String blQty;
    private String netBuyQty;
    private String netSellQty;
    private String netBuyAvgPrice;
    private String netSellAvgPrice;
}
