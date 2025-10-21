package com.sendByOP.expedition.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.zalando.problem.Status;

@Getter
public enum ErrorInfo {

    // Resource validation errors
    REFERENCE_RESOURCE_REQUIRED("This field is required", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("Resource not found", HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS("This resource already exists", HttpStatus.BAD_REQUEST),
    INVALID_RESOURCE_DATA("Invalid resource data provided", HttpStatus.BAD_REQUEST),
    
    // Authentication and registration errors
    EMAIL_ALREADY_EXISTS("Email address is already registered", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND("User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED("Account has been locked. Please contact support", HttpStatus.FORBIDDEN),
    ACCOUNT_NOT_VERIFIED("Account email not verified", HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED("Email not verified. Please check your inbox for verification link", HttpStatus.FORBIDDEN),
    ACCOUNT_BLOCKED("Account has been blocked. Please contact support", HttpStatus.FORBIDDEN),
    ACCOUNT_INACTIVE("Account is inactive. Please reactivate your account", HttpStatus.FORBIDDEN),
    ACCOUNT_PENDING_VERIFICATION("Account is pending verification. Please verify your email", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED("Verification token has expired", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID("Invalid verification token", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("Invalid password format", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_OLD("New password must be different from the current password", HttpStatus.BAD_REQUEST),
    
    // Email related errors
    EMAIL_SEND_ERROR("Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_EMAIL_FORMAT("Invalid email format", HttpStatus.BAD_REQUEST),
    
    // Payment and booking errors
    PAYMENT_REQUIRED("Payment is required to proceed", HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_ALREADY_PROCESSED("Payment has already been processed", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED("Payment processing failed", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_AMOUNT("Invalid payment amount", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND("Booking not found", HttpStatus.NOT_FOUND),
    BOOKING_ALREADY_CANCELLED("Booking has already been cancelled", HttpStatus.BAD_REQUEST),
    OPERATION_NOT_ALLOWED("This operation is not allowed", HttpStatus.FORBIDDEN),
    
    // Parcel and shipping errors
    PARCEL_CREATION_FAILED("Failed to create parcel", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PARCEL_WEIGHT("Invalid parcel weight", HttpStatus.BAD_REQUEST),
    INVALID_SHIPPING_ADDRESS("Invalid shipping address", HttpStatus.BAD_REQUEST),
    SHIPPING_NOT_AVAILABLE("Shipping service not available for this destination", HttpStatus.BAD_REQUEST),
    
    // General errors
    INTERNAL_ERROR("An internal error occurred. Please try again later", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("Validation error occurred", HttpStatus.BAD_REQUEST),
    SERVICE_UNAVAILABLE("Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    UNEXPECTED_ERROR("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorInfo(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
