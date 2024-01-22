package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeBookResponse {

    @JsonProperty("Tsym")
    public String tsym;

    @JsonProperty("Time")
    public String time;

    @JsonProperty("Prctype")
    public String priceType;

    @JsonProperty("Pcode")
    public String productCode;

    @JsonProperty("Price")
    public String price;

    @JsonProperty("Qty")
    public Integer quantity;

    @JsonProperty("stat")
    public String status;

    @JsonProperty("Exchange")
    public String exchange;

    @JsonProperty("Trantype")
    public String transactionType;

    @JsonProperty("companyname")
    public String companyName;
}
