package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.BadRequestException;

public class SelfRequestException extends BadRequestException {
    public SelfRequestException() {
        super("This is you");
    }
}
