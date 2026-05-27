package com.bookhub.authservice.handlers;

import com.bookhub.authservice.dtos.responses.ErrorResponseDto;
import com.bookhub.authservice.exceptions.BadRequestException;
import com.bookhub.authservice.exceptions.NotFoundException;
import com.bookhub.authservice.exceptions.TooManyRequestsException;
import com.bookhub.authservice.exceptions.extensions.IncorrectRegisterDataException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundExceptions(NotFoundException e){
        return new ResponseEntity<>(new ErrorResponseDto(e.getMessage(), Instant.now(),404),HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException e){
        var response = new ResponseEntity<>(e.getReason(),e.getStatusCode());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response;
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestExceptions(BadRequestException e){
        return new ResponseEntity<>(new ErrorResponseDto(e.getMessage(), Instant.now(),400),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(){
        return new ResponseEntity<>(new ErrorResponseDto("Incorrect password", Instant.now(),401),HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IncorrectRegisterDataException.class)
    public ResponseEntity<ErrorResponseDto> handleIncorrectRegisterDataException(IncorrectRegisterDataException e){
        return new ResponseEntity<>(new ErrorResponseDto(e.getMessage(), Instant.now(),400),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponseDto> handleTooManyRequestsException(TooManyRequestsException e){
        return new ResponseEntity<>(new ErrorResponseDto(e.getMessage(), Instant.now(),429),HttpStatus.TOO_MANY_REQUESTS);
    }

}
