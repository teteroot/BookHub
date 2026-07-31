package com.bookhub.profileservice.exceptions;

public abstract class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String message) {
        super(message);
    }
}
