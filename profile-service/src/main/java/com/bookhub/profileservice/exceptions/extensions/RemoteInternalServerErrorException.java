package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.InternalServerErrorException;

public class RemoteInternalServerErrorException extends InternalServerErrorException {
    public RemoteInternalServerErrorException() {
        super("Remote server is unavailable");
    }
}
