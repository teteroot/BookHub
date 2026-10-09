package com.bookhub.bookservice.dtos.responses;

import java.time.Instant;

public record ErrorResponseDto(String message, Instant time, Integer statusCode) {
}
