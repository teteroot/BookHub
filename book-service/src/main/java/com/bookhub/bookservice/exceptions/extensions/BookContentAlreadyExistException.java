package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;

public class BookContentAlreadyExistException extends BadRequestException {
    public BookContentAlreadyExistException() {
        super("Book content already exists");
    }
}
