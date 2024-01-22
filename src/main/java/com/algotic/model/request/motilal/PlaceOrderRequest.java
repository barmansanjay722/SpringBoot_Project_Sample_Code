package com.algotic.model.request.motilal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * This class generate motilal specific order request
 *
 */
@Data
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {
    private String exchange;

    private Integer symboltoken;

    @JsonProperty("buyorsell")
    private String buyorsell;

    @JsonProperty("ordertype")
    private String ordertype;

    @JsonProperty("producttype")
    private String producttype;

    @JsonProperty("orderduration")
    private String orderduration;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("triggerprice")
    private Double triggerprice;

    private Integer quantityinlot;

    @JsonProperty("disclosedquantity")
    private Integer disclosedquantity;

    @JsonProperty("amoorder")
    private String amoorder;

    private String algoid;

    private String goodtilldate;

    private String tag;

    private String participantcode;
}
