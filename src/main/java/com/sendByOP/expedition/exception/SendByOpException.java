package com.sendByOP.expedition.exception;

import org.springframework.http.HttpStatus;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * The Class SendByOpException.
 */
@Data
public class SendByOpException extends Exception {

    private static final long serialVersionUID = 1L;
    private final HttpStatus httpStatus;
    private List<String> messages;
    private ErrorInfo errorInfo;


    public SendByOpException(ErrorInfo errorInfo, String... messages) {
        super(errorInfo.getMessage());
        this.httpStatus = errorInfo.getHttpStatus();
        this.messages = Arrays.asList(messages);
        this.errorInfo = errorInfo;
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
