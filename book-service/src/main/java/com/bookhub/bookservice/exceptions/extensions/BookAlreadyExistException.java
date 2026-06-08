package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;

public class BookAlreadyExistException extends BadRequestException {
    public BookAlreadyExistException(String title) {
        super("Book with title %s is already exist".formatted(title));
    }
}
