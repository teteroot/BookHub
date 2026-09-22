package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.BadRequestException;

public class IncorrectUserHeadersException extends BadRequestException {
    public IncorrectUserHeadersException() {
        super("Incorrect user id or user role");
    }
}
