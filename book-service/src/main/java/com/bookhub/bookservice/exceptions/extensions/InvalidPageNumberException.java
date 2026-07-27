package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;

public class InvalidPageNumberException extends BadRequestException {
    public InvalidPageNumberException() {
        super("Invalid page number");
    }
}
