package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.InternalServerErrorException;
import org.springframework.http.HttpStatus;

public class RemoteInternalServerErrorException extends InternalServerErrorException {
    public RemoteInternalServerErrorException(HttpStatus status) {
        super("Remote server error", status);
    }
}
