package com.sendByOP.expedition.exception;

import org.springframework.http.HttpStatus;
import org.zalando.problem.Status;

import java.util.Arrays;
import java.util.List;

/**
 * The Class HotelTechnicalException.
 */
public class SendByOpException extends Exception {

    private static final long serialVersionUID = 1L;
    private final HttpStatus httpStatus;
    private List<String> messages;


    public SendByOpException(ErrorInfo errorInfo, String... messages) {
        super(errorInfo.getMessage());
        this.httpStatus = errorInfo.getHttpStatus();
        this.messages = Arrays.asList(messages);
    }

    /**
     * @param msg
     * @param httpStatus
     */
    public SendByOpException(String msg, HttpStatus httpStatus) {
        super(msg);
        this.httpStatus = httpStatus;
    }

    /**
     * @param cause
     * @param httpStatus
     */
    public SendByOpException(Throwable cause, HttpStatus httpStatus) {
        super(cause);
        this.httpStatus = httpStatus;
    }

    /**
     * @return the httpStatus
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }


}
