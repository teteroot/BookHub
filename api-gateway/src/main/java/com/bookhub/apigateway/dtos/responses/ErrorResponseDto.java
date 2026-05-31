package com.bookhub.apigateway.dtos.responses;

import java.time.Instant;

public record ErrorResponseDto(String message, Instant time, Integer statusCode) {
}
