package com.algotic.model.response;

import com.algotic.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private String orderRefNumber;
    private String orderStatus;
    private String errorMessage;
    private String errorMsge;
    private boolean isException;
    private ErrorCode errorCode;
}
