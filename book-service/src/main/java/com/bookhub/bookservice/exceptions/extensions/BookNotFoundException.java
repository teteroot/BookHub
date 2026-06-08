package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.NotFoundException;

public class BookNotFoundException extends NotFoundException {
    public BookNotFoundException() {
        super("Book not found");
    }
}
