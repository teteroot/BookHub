package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;

public class BookArchiveEmptyException extends BadRequestException {
    public BookArchiveEmptyException() {
        super("Book archive is empty");
    }
}
