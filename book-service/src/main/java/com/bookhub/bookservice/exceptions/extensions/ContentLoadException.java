package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.InternalServerErrorException;

public class ContentLoadException extends InternalServerErrorException {
    public ContentLoadException() {
        super("Failed to load book content");
    }
}
