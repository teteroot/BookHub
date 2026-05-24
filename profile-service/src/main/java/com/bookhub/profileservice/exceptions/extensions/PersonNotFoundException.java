package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.NotFoundException;

public class PersonNotFoundException extends NotFoundException {
    public PersonNotFoundException() {
        super("Person not found");
    }
}
