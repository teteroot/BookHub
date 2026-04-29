package com.bookhub.authservice.exceptions.extensions;

import com.bookhub.authservice.exceptions.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("User not found");
    }
}
