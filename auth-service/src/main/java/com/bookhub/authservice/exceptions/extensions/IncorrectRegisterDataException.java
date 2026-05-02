package com.bookhub.authservice.exceptions.extensions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

public class IncorrectRegisterDataException extends RuntimeException {
    @Getter
    private final String message;
    public IncorrectRegisterDataException(BindingResult result) {
        StringBuilder errorMessage = new StringBuilder();
        result.getFieldErrors()
                .forEach((error) -> {
                    errorMessage.append(error.getDefaultMessage());
                    errorMessage.append("; ");
                });
        message = errorMessage.toString();
    }
}
