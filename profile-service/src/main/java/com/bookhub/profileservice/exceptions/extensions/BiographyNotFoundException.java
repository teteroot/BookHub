package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.NotFoundException;

public class BiographyNotFoundException extends NotFoundException {
    public BiographyNotFoundException() {
        super("Biography not found");
    }
}
