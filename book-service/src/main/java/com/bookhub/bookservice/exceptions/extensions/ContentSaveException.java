package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.InternalServerErrorException;

public class ContentSaveException extends InternalServerErrorException {
    public ContentSaveException() {
        super("Failed to save book content");
    }
}
