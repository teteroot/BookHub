package com.bookhub.bookservice.handlers;

import com.bookhub.bookservice.dtos.responses.ErrorResponseDto;
import com.bookhub.bookservice.exceptions.BadRequestException;
import com.bookhub.bookservice.exceptions.ForbiddenException;
import com.bookhub.bookservice.exceptions.InternalServerErrorException;
import com.bookhub.bookservice.exceptions.NotFoundException;
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

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbiddenException(ForbiddenException e) {
        var errorResponse = new ErrorResponseDto(e.getMessage(), Instant.now(), 403);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponseDto> handleInternalServerErrorException(InternalServerErrorException e) {
        var errorResponse = new ErrorResponseDto(e.getMessage(), Instant.now(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
