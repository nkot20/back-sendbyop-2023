package com.sendByOP.expedition.web.exceptions.vol;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VolNotFoundException extends RuntimeException{
    public VolNotFoundException(String message){ super(message); }
}
