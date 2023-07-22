package com.sendByOP.expedition.web.exceptions.escale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ImpossibleEffectuerEscaleException extends RuntimeException{
    public ImpossibleEffectuerEscaleException(String message) { super(message); }
}
