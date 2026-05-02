package com.bookhub.authservice.exceptions.extensions;

import com.bookhub.authservice.exceptions.BadRequestException;

public class EmailIsAlreadyUsedException extends BadRequestException {
    public EmailIsAlreadyUsedException(String email) {
        super("Email %s is already in use".formatted(email));
    }
}
