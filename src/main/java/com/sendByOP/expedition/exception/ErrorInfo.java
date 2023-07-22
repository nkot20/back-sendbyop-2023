package com.sendByOP.expedition.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.zalando.problem.Status;

@Getter
public enum ErrorInfo {

    REFERENCE_RESSOURCE_REQUIRED("Ce champ est obligatoire", HttpStatus.BAD_REQUEST),
    RESSOURCE_NOT_FOUND("Ressource introuvable", HttpStatus.NOT_FOUND),
    REFERENCE_RESSOURCE_ALREADY_USED("L'élément est déja utilisé", HttpStatus.BAD_REQUEST),
    INTRERNAL_ERROR("Un problème est survenu veillez réessayer plutard", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorInfo(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
