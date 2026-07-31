package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.UnsupportedMediaTypeException;

public class UnsupportedImageAspectRatio extends UnsupportedMediaTypeException {
    public UnsupportedImageAspectRatio(int width, int height) {
        super("Images with resolution %s x %s is not supported".formatted(width,height));
    }
}
