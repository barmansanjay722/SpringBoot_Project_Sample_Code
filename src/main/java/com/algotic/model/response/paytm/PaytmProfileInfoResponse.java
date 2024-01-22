package com.algotic.model.response.paytm;

import lombok.Data;

@Data
public class PaytmProfileInfoResponse {

    private PaytmProfileInfoData data;

    private PaytmProfileInfoMetaData meta;
}
