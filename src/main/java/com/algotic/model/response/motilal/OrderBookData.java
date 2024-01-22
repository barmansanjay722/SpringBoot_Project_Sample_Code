package com.algotic.model.response.motilal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookData {

    private String ordercategory;
    private String uniqueorderid;
    private String clientid;
    private String exchange;
    private String symboltoken;
    private String symbol;
    private String series;
    private String expirydate;
    private String strikeprice;
    private String ordertype;
    private String orderduration;
    private String producttype;
    private String error;
    private String orderstatus;
    private String buyorsell;
    private BigDecimal price;

    @JsonProperty("triggerprice")
    private BigDecimal triggerPrice;

    private BigDecimal averageprice;
    private Integer orderqty;
    private String recordinserttime;
}
