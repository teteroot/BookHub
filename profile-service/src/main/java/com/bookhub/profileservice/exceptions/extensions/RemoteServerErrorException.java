package com.bookhub.profileservice.exceptions.extensions;

import com.bookhub.profileservice.exceptions.InternalServerErrorException;
import org.springframework.http.HttpStatus;

public class RemoteServerErrorException extends InternalServerErrorException {
    public RemoteServerErrorException(HttpStatus status) {
        super("Remote server error", status);
    }
}
