package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.ConflictException;

public class PageConcurrentModificationException extends ConflictException {
    public PageConcurrentModificationException() {
        super("Page is uploading now");
    }
}
