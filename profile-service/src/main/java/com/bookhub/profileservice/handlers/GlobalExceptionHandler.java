package com.bookhub.profileservice.handlers;


import com.bookhub.profileservice.dtos.responses.ErrorResponseDto;
import com.bookhub.profileservice.exceptions.extensions.IncorrectRequestDataException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IncorrectRequestDataException.class)
    public ResponseEntity<ErrorResponseDto> handleIncorrectRequestDataException(IncorrectRequestDataException e) {
        var errorResponse = new ErrorResponseDto(e.getMessage(), Instant.now(), 400);
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
