package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;

public class TooManyPagesException extends BadRequestException {
    public TooManyPagesException(Integer countOfPages) {
        super("Uploaded content must contain exactly %d page".formatted(countOfPages));
    }
}
