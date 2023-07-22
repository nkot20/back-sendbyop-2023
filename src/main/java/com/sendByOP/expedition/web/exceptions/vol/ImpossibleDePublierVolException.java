package com.sendByOP.expedition.web.exceptions.vol;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ImpossibleDePublierVolException extends RuntimeException{
    public ImpossibleDePublierVolException(String message){ super(message); }
}
