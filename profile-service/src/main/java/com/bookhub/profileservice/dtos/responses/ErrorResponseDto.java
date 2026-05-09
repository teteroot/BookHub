package com.bookhub.profileservice.dtos.responses;

import java.time.Instant;

public record ErrorResponseDto(String message, Instant time, Integer statusCode) {
}
