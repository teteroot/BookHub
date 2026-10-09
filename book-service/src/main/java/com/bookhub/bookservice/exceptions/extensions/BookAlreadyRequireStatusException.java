package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.enums.BookStatus;
import com.bookhub.bookservice.exceptions.BadRequestException;

public class BookAlreadyRequireStatusException extends BadRequestException {
    public BookAlreadyRequireStatusException(BookStatus require) {
        super("The Book already has %s status".formatted(require.name()));
    }
}
