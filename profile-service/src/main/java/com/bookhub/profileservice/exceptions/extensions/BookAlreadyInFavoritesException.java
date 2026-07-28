package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.BadRequestException;

public class BookAlreadyInFavoritesException extends BadRequestException {
    public BookAlreadyInFavoritesException() {
        super("Book already in favorites");
    }
}
