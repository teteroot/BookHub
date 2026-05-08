package com.bookhub.authservice.exceptions;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException() {
        super("Too many requests. Please try again later.");
    }
}
