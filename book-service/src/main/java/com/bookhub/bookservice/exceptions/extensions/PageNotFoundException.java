package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.NotFoundException;

public class PageNotFoundException extends NotFoundException {
    public PageNotFoundException() {
        super("Page not found");
    }
}
