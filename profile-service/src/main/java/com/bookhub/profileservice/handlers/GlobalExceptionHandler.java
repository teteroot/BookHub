package com.bookhub.profileservice.handlers;


import com.bookhub.profileservice.dtos.responses.ErrorResponseDto;
import com.bookhub.profileservice.exceptions.BadRequestException;
import com.bookhub.profileservice.exceptions.InternalServerErrorException;
import com.bookhub.profileservice.exceptions.NotFoundException;
import com.bookhub.profileservice.exceptions.extensions.RemoteServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var result = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();
        result.getFieldErrors()
                .forEach((error) -> {
                    errorMessage.append(error.getDefaultMessage());
                    errorMessage.append("; ");
                });
        var errorResponse = new ErrorResponseDto(errorMessage.toString(), Instant.now(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(NotFoundException e) {
        var errorResponse = new ErrorResponseDto(e.getMessage(), Instant.now(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException e) {
        var errorResponse = new ErrorResponseDto(e.getMessage(), Instant.now(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException e) {
        var errorResponse = new ErrorResponseDto(e.getMessage(), Instant.now(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ResponseEntity<String> handleRemoteServiceException(RemoteServiceException e) {
        return ResponseEntity.status(e.getStatusCode()).body(e.getBody());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponseDto> handleInternalServerErrorException(InternalServerErrorException e) {
        var errorResponse = new ErrorResponseDto(e.getMessage(), Instant.now(), e.getInternalErrorStatus().value());
        return ResponseEntity.status(e.getInternalErrorStatus()).body(errorResponse);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponseDto> handleCallNotPermittedException(CallNotPermittedException e) {
        var errorResponse = new ErrorResponseDto("Remote server is unavailable", Instant.now(), 503);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

}
