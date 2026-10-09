package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.InternalServerErrorException;

public class CoverLoadException extends InternalServerErrorException {
    public CoverLoadException() {
        super("Failed to load book cover");
    }
}
