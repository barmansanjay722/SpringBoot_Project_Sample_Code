package com.algotic.model.response.paytm;

import lombok.Data;

@Data
public class PaytmProfileInfoMetaData {

    private String requestId;

    private String responseId;

    private String code;

    private String message;

    private String displayMessage;
}
