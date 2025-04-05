package com.sendByOP.expedition.exception;

public class PhoneVerificationException extends RuntimeException {
    private final String errorCode;

    public PhoneVerificationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static class ErrorCodes {
        public static final String INVALID_PHONE_NUMBER = "INVALID_PHONE";
        public static final String INVALID_CODE = "INVALID_CODE";
        public static final String VERIFICATION_FAILED = "VERIFICATION_FAILED";
        public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    }
}