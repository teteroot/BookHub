package com.bookhub.bookservice.exceptions;

public abstract class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
