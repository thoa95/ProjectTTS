package com.example.bookingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SeatRegistrationForbidden extends RuntimeException {
    public SeatRegistrationForbidden(String message) {
        super(message);
    }
}