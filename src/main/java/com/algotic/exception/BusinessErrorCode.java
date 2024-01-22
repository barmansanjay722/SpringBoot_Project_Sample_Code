package com.algotic.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum BusinessErrorCode implements ErrorCode {
    BROKER_NOT_EXISTS(100018, "BROKER_NOT_EXISTS", "broker is not exists", HttpStatus.NOT_FOUND),
    TRADE_IS_NOT_ACTIVE(100019, "TRADE_IS_NOT_ACTIVE", "trade id not active", HttpStatus.NOT_FOUND),
    SESSION_ID_NOT_EXISTS(100020, "Session_Id_Not_Exist", "Session not exist", HttpStatus.BAD_REQUEST),
    USER_ID_NOT_VALID(100021, "USER_ID_NOT_VALID", "User id not valid", HttpStatus.BAD_REQUEST),
    STRATEGY_NOT_EXISTS(100022, "STRATEGY_NOT_EXISTS", "Strategy not found", HttpStatus.NOT_FOUND),
    ID_NOT_EXISTS(100023, "ID_NOT_FOUND", "Id not found", HttpStatus.NOT_FOUND),
    BROKER_DETAILS_NOT_FOUND(100024, "BROKER_DETAILS_NOT_FOUND", "BROKER_DETAILS_NOT_FOUND", HttpStatus.NOT_FOUND),
    WATCHLIST_NOT_FOUND(100025, "WATCHLIST_NOT_FOUND", "Watch list not found", HttpStatus.NOT_FOUND),
    STRATEGY_ALREADY_EXIST(100026, "STRATEGY_NAME_EXIST", "Strategy Name already exist", HttpStatus.CONFLICT),
    INVALID_APP_CODE(100027, "INVALID_APPCODE", "App code is invalid", HttpStatus.NOT_FOUND),
    BROKER_SESSION_EXPIRED(1000028, "BROKER_SESSION_EXPIRED", "Broker session expired", HttpStatus.BAD_REQUEST),
    STATUS_NOT_EXISTS(1000029, "STATUS_NOT_EXISTS", "Invalid Status", HttpStatus.NOT_FOUND),
    COMPANY_TYPE_NOT_EXIST(1000030, "COMPANY_TYPE_NOT_EXIST", "invalid company type", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_EXIST(1000031, "TOKEN_NOT_EXIST", "token not found", HttpStatus.NOT_FOUND),

    SOMETHING_WENT_WRONG(
            1000032,
            "SOMETHING_WENT_WRONG",
            "something went wrong while place order in alice blue",
            HttpStatus.INTERNAL_SERVER_ERROR),
    EDIS_AUTHORIZATION_REQUIRED(
            1000032, "EDIS_AUTHORIZATION_REQUIRED", "EDIS Authorization Required", HttpStatus.NOT_FOUND),
    SUBSCRIBER_NOT_EXIST(
            1000033,
            "SUBSCRIBER_NOT_EXIST",
            "This user Is not Subscribe the Algotic Subscription",
            HttpStatus.NOT_FOUND),
    TRADE_NOT_EXISTS(100034, "TRADE_NOT_EXISTS", "trade not exists", HttpStatus.NOT_FOUND),
    TRADE_TYPE_NOT_EXISTS(100035, "TRADE_TYPE_NOT_EXISTS", "trade type not exists", HttpStatus.NOT_FOUND),
    INVALID_SUBSCRIPTION_ID(100036, "SUBSCRIPTION_ID_INVALID", "Subscription Id is not Valid", HttpStatus.NOT_FOUND),
    BROKER_REQUEST_IN_PROGRESS(
            100037, "BROKER_REQUEST_IN_PROGRESS", "Broker request in progress", HttpStatus.BAD_REQUEST),
    STATUS_ALREADY_CREATED(100038, "STATUS_ALREADY_CREATED", "status already created", HttpStatus.BAD_REQUEST);

    private final int errorId;
    private final String errorCode;
    private String errorMessage;
    private final HttpStatus httpStatus;

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
