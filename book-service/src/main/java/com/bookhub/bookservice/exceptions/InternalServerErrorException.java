package com.bookhub.bookservice.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public abstract class InternalServerErrorException extends RuntimeException {
    @Getter
    private final HttpStatus internalErrorStatus;

    public InternalServerErrorException(String message, HttpStatus internalErrorStatus) {
        super(message);
        this.internalErrorStatus = internalErrorStatus;
    }

    public InternalServerErrorException(String message) {
        super(message);
        this.internalErrorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
