package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.ConflictException;

public class BookConcurrentModificationException extends ConflictException {
    public BookConcurrentModificationException() {
        super("Book is uploading now");
    }
}
