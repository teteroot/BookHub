package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.BadRequestException;

public class BookNotInFavoritesException extends BadRequestException {
    public BookNotInFavoritesException() {
        super("Book is not in favorites");
    }
}
