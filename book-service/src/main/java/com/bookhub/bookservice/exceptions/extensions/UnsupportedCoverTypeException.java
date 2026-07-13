package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.BadRequestException;
import org.springframework.http.MediaType;

public class UnsupportedCoverTypeException extends BadRequestException {
    public UnsupportedCoverTypeException(MediaType type) {
        super("Type %s is not supported".formatted(type.toString()));
    }
}
