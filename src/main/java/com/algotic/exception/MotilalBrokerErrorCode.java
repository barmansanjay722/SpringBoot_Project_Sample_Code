package com.algotic.exception;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum MotilalBrokerErrorCode implements ErrorCode {
    INVALID_TOKEN(30001, "MOTILAL_INVALID_TOKEN", "Invalid Token", HttpStatus.UNAUTHORIZED, "MO8001"),

    INVALID_PRODUCT_TYPE(
            30002, "MOTILAL_INVALID_PRODUCT_TYPE", "Invalid Product Type", HttpStatus.INTERNAL_SERVER_ERROR, "MO1057"),

    MOTILAL_INTERNAL_SERVER_ERROR(
            30003, "MOTILAL_INTERNAL_SERVER_ERROR", "Something went wrong...", HttpStatus.INTERNAL_SERVER_ERROR, ""),

    ORDER_ALREADY_CANCEL(30004, "ORDER_ALREADY_CANCEL", "Order Is Already Cancelled...", HttpStatus.CONFLICT, "MO1066"),

    INVALID_TRIGGER_COMBINATION(
            30005,
            "INVALID_TRIGGER_INPUT",
            "Invalid Trigger Price Order Type Combination",
            HttpStatus.CONFLICT,
            "MO1079"),

    MARKET_CLOSED(30005, "MARKET_CLOSED", "Security is not allowed, market is closed...", HttpStatus.CONFLICT, "16387"),

    SHORT_SELLING_NOT_ALLOWD(
            30006,
            "SHORT_SELLING_NOT_ALLOWD",
            "Short selling not allowd for this trade .....",
            HttpStatus.CONFLICT,
            "100031");

    private final int errorId;
    private final String errorCode;
    private String errorMessage;
    private final HttpStatus httpStatus;
    private String refId;

    public static MotilalBrokerErrorCode getErrorCodeByRefId(String refId) {
        List<MotilalBrokerErrorCode> res = Arrays.asList(MotilalBrokerErrorCode.values());
        return res.stream().filter(m -> m.refId.equals(refId)).findFirst().orElse(MOTILAL_INTERNAL_SERVER_ERROR);
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
