package com.bookhub.bookservice.exceptions.extensions;

import com.bookhub.bookservice.exceptions.InternalServerErrorException;
import org.springframework.http.HttpStatus;

public class RemoteInternalServerErrorException extends InternalServerErrorException {
    public RemoteInternalServerErrorException(HttpStatus status) {
        super("Remote server error", status);
    }
}
