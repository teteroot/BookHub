package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.BadRequestException;

public class BookNotDraftingException extends BadRequestException {

    public BookNotDraftingException() {
        super("Book status isn't \"Draft\"");
    }

    public BookNotDraftingException(BookStatus status) {
        super(switch (status) {
            case EMPTY -> "Book is empty";
            case ARCHIVED -> "Book is archived";
            case PUBLISHED -> "Book is already published";
            default -> "draft logic error";
        });
    }
}
