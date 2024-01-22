package com.algotic.model.response.motilal;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeBookData {

    private String uniqueorderid;
    private String clientid;
    private String exchange;
    private String symboltoken;
    private String symbol;
    private String instrumenttype;
    private String series;
    private String strikeprice;
    private String optiontype;
    private String expirydate;
    private Integer lotsize;
    private Integer precision;
    private Integer multiplier;
    private Integer tradeqty;
    private BigDecimal tradeprice;
    private BigDecimal tradevalue;
    private String producttype;
    private String tradeno;
    private String buyorsell;
    private String orderid;
    private String tradetime;
}
