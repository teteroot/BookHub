package com.bookhub.bookservice.exceptions;

public abstract class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String message) {
        super(message);
    }
}
