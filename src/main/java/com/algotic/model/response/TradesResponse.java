package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradesResponse {
    private String id;
    private String instrument;
    private String tradeType;
    private String stockType;
    private String exchange;
    private String strategyName;
    private Boolean isActive;
    private Integer lotSize;
    private Boolean stragegyIsActive;
    private String orderType;
    private Double stopLossPrice;
    private Double targetProfit;
}
