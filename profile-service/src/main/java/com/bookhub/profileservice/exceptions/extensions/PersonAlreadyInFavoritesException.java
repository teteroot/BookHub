package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.BadRequestException;

public class PersonAlreadyInFavoritesException extends BadRequestException {
    public PersonAlreadyInFavoritesException() {
        super("Person already in favorites");
    }
}
