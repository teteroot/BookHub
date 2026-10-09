package com.bookhub.bookservice.exceptions.extensions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class RemoteServiceException extends RuntimeException {

    private final String body;

    private final HttpStatusCode statusCode;

    public RemoteServiceException(String body, HttpStatusCode code) {
        this.body = body;
        this.statusCode = code;
    }
}
