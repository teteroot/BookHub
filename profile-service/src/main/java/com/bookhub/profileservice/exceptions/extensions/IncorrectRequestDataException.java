package com.bookhub.profileservice.exceptions.extensions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

public class IncorrectRequestDataException extends RuntimeException {
    @Getter
    private final String message;
    public IncorrectRequestDataException(BindingResult result) {
        StringBuilder errorMessage = new StringBuilder();
        result.getFieldErrors()
                .forEach((error) -> {
                    errorMessage.append(error.getDefaultMessage());
                    errorMessage.append("; ");
                });
        message = errorMessage.toString();
    }
}
