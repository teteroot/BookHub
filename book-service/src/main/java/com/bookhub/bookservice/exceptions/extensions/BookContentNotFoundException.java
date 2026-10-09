package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.NotFoundException;

public class BookContentNotFoundException extends NotFoundException {
    public BookContentNotFoundException() {
        super("Book content not found");
    }
}
