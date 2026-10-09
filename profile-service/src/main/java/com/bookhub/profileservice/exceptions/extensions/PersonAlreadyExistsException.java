package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.BadRequestException;

public class PersonAlreadyExistsException extends BadRequestException {
    public PersonAlreadyExistsException() {
        super("Person already exists");
    }
}
