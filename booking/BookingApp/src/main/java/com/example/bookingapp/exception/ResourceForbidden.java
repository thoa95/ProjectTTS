package com.example.bookingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ResourceForbidden extends RuntimeException {
    public ResourceForbidden(String message) {
        super(message);
    }
}