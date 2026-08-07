package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.InternalServerErrorException;

public class RemoteInternalServerErrorException extends InternalServerErrorException {
    public RemoteInternalServerErrorException() {
        super("Remote server is unavailable");
    }
}
