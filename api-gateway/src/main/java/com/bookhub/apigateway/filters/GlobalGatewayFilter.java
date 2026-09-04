package com.bookhub.apigateway.filters;

import com.bookhub.apigateway.dtos.responses.ErrorResponseDto;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public abstract class GlobalGatewayFilter {
    protected Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status, ObjectMapper objectMapper) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var errorResponse = new ErrorResponseDto(message, Instant.now(),status.value());
        byte[] bytes = objectMapper.writeValueAsString(errorResponse).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
