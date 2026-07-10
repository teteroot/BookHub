package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;

public class BookNotDraftingException extends BadRequestException {
    public BookNotDraftingException() {
        super("Book status isn't \"Draft\"");
    }
}
