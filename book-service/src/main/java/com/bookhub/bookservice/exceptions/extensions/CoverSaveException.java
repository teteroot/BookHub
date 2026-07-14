package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.InternalServerErrorException;

public class CoverSaveException extends InternalServerErrorException {
    public CoverSaveException() {
        super("Failed to save book cover");
    }
}
