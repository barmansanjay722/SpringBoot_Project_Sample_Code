package com.algotic.model.request.paytm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaytmCancelOrderRequest {

    private String source;

    @JsonProperty("txn_type")
    private String trnxType;

    private String exchange;

    private String segment;

    private String product;

    @JsonProperty("security_id")
    private String securityId;

    private Integer quantity;

    private String validity;

    @JsonProperty("order_type")
    private String orderType;

    private BigDecimal price;

    @JsonProperty("off_mkt_flag")
    private String offMarketFlag;

    @JsonProperty("mkt_type")
    private String marketType;

    @JsonProperty("order_no")
    private String orderNumber;

    @JsonProperty("serial_no")
    private Integer serialNumber;

    @JsonProperty("group_id")
    private Integer groupId;

    @JsonProperty("trigger_price")
    private BigDecimal triggerPrice;
}
