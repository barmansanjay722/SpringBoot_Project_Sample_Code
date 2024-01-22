package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HoldingAlgoticResponse {
    private String instrumentName;
    private String tradingSymbol;
    private String tradeType;
    private String orderType;
    private String quantity;
    private String price;
    private String ltp;
    private String token;
    private String exchange;
    private String pdc;
    private Boolean isStock;
}
