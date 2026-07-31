package com.bookhub.bookservice.exceptions;

public abstract class UnsupportedMediaTypeException extends RuntimeException {
    public UnsupportedMediaTypeException(String message) {
        super(message);
    }
}
