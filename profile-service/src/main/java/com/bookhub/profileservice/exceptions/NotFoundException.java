package com.bookhub.profileservice.exceptions;

public abstract class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) {
        super(msg);
    }
}