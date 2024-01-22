package com.algotic.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR(100001, "INTERNAL_SERVER_ERROR", "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_ALREADY_EXISTS(100002, "EMAIL_ALREADY_EXISTS", "Email id already exists", HttpStatus.CONFLICT),
    CUSTOMER_NOT_EXISTS(100003, "CUSTOMER_NOT_EXISTS", "customer id not exists", HttpStatus.NOT_FOUND),
    DATA_NOT_SAVED(100004, "DATA_NOT_SAVED", "data not saved", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_NOT_FOUND(100005, "DATA_NOT_FOUND", "data not found", HttpStatus.NOT_FOUND),
    USERNAME_NOT_VALID(100006, "USERNAME_NOT_VALID", "username not valid", HttpStatus.BAD_REQUEST),

    BAD_REQUEST(100007, "BAD_REQUEST", "Wrong Data", HttpStatus.BAD_REQUEST),
    PHONENUMBER_ALREADY_EXISTS(10008, "PHONE_NUMBER_EXISTS", "Phone number already exists", HttpStatus.CONFLICT),
    TERMS_ACCEPTED_NOT_VALID(10009, "Terms_Accepted_Is_Not_False", "WRONG INPUT", HttpStatus.BAD_REQUEST),
    INVALID_PRICE_TYPE(100010, "PRICE_TYPE_NOT_EXISTS", "Price Type not found", HttpStatus.NOT_FOUND),
    INVALID_PRODUCT_CODE(100011, "PRODUCT_CODE_NOT_EXISTS", "Product code not found", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_TYPE(100012, "TRANSACTION_TYPE_NOT_EXISTS", "Transaction Type not found", HttpStatus.NOT_FOUND),
    TYPE_NOT_EXISTS(
            100013, "INVALID_LOGIN_TYPE", "Login can be through from BROKER either ALGOTIC", HttpStatus.NOT_FOUND),
    INVALID(100014, "INVALID_OTP", "Invalid otp", HttpStatus.BAD_REQUEST),
    LIMIT(100015, "LIMIT", "Limit && Offset is greater than or equals to 0", HttpStatus.BAD_REQUEST),
    FORBIDDEN_API(1000016, "FORBIDDEN_API", "Forbidden", HttpStatus.FORBIDDEN),
    TOKEN_NOT_FOUND(
            1000017,
            " PLEASE_CHECK_CUSTOMER_ID",
            "Access token doesn't belong to this customer id",
            HttpStatus.BAD_REQUEST),

    ACCESS_TOKEN_NOT_FOUND(1000018, "ACCESS_TOKEN_NOT_FOUND", "access token not found", HttpStatus.NOT_FOUND),
    PASSWORD_NOT_VALID(1000019, "PASSWORD_INCORRECT", "password incorrect", HttpStatus.BAD_REQUEST),
    PDF_ALREADY_EXISTS(1000020, "PDF_EXISTS", "pdf exists", HttpStatus.BAD_REQUEST),
    ADMIN_STATUS_CANNOT_CHANGE(
            1000021, "ADMIN_STATUS_CANNOT_CHANGE", " Admin status can't change", HttpStatus.BAD_REQUEST),
    BLOCKED_STATUS_CHECK(
            1000021,
            "USER IS BLOCKED",
            "User is blocked, Please contact with System Administrator",
            HttpStatus.BAD_REQUEST),
    INACTIVE_STATUS_CHECK(
            1000021,
            "USER IS INACTIVE",
            "User is inactive, Please contact with System Administrator",
            HttpStatus.BAD_REQUEST),
    BROKER_CUSTOMER_NOT_LINKED(
            1000022, "BROKER_CUSTOMER_NOT_LINKED", "Customer is not linked with broker", HttpStatus.BAD_REQUEST),
    PAPER_TRADE_ERROR(
            1000023,
            " PAPER_TRADE_ERROR",
            "Order will not be placed as we are not able to fetch latest Price",
            HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TRADE_TYPE(1000024, "INVALID_TRADE_TYPE", "Trade Type not found", HttpStatus.NOT_FOUND),
    INVALID_CONTACTUS(1000025, "INVALID_CONTACTUS_REQUEST", "All fields are mandatory", HttpStatus.NOT_FOUND),
    RESEND_OTP(1000026, "RESEND_OTP", "Attempts multiple time please resend otp", HttpStatus.BAD_REQUEST);

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
