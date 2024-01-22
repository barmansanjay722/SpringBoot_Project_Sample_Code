package com.algotic.exception;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum PaytmErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(40000, "", "", HttpStatus.INTERNAL_SERVER_ERROR, ""),

    ORDER_STATUS_CHANGED(40001, "PAYTM_INVALID_STATUS_TYPE", "Invalid Status Type", HttpStatus.BAD_REQUEST, "RS-0022"),

    MARKET_CLOSED(40003, "MARKET_CLOSED", "Market is closed", HttpStatus.BAD_REQUEST, "O3052"),

    NSE_MARKET_CLOSED(40004, "NSE_MARKET_CLOSED", "Nse Market is closed", HttpStatus.BAD_REQUEST, "3309");

    private final int errorId;
    private final String errorCode;
    private String errorMessage;
    private final HttpStatus httpStatus;
    private String refId;

    public static PaytmErrorCode getErrorCodeByRefId(String refId) {
        List<PaytmErrorCode> res = Arrays.asList(PaytmErrorCode.values());
        return res.stream()
                .filter(p -> p.refId.equalsIgnoreCase(refId))
                .findFirst()
                .orElse(INTERNAL_SERVER_ERROR);
    }

    @Override
    public int getErrorId() {
        return errorId;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
