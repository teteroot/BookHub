package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.ForbiddenException;

public class BookAccessDeniedException extends ForbiddenException {
    public BookAccessDeniedException() {
        super("This is not your book");
    }
}
