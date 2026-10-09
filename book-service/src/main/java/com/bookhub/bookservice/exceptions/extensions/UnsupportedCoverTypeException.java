package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.UnsupportedMediaTypeException;
import org.springframework.http.MediaType;

public class UnsupportedCoverTypeException extends UnsupportedMediaTypeException {
    public UnsupportedCoverTypeException(MediaType type) {
        super("Type %s is not supported".formatted(type.toString()));
    }
    public UnsupportedCoverTypeException() {
        super("This type is not supported");
    }
}
