package com.algotic.exception;

public class AlgoticException extends RuntimeException {
    private final ErrorCode errorCode;

    public AlgoticException(ErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
