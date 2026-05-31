package com.bookhub.apigateway.filters;

import com.bookhub.apigateway.dtos.responses.ErrorResponseDto;
import com.bookhub.apigateway.security.JwtParser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private final String REQUEST_HEADER_NAME = "Authorization";
    private final String REQUEST_HEADER_PREFIX = "Bearer ";
    private final String USER_ID_HEADER_NAME = "X-User-Id";
    private final String USER_ROLE_HEADER_NAME = "X-User-Role";
    private final JwtParser jwtParser;
    private final ObjectMapper objectMapper;

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var token = request.getHeaders().getOrEmpty(REQUEST_HEADER_NAME).stream().findFirst();
        if (token.isPresent() && token.get().startsWith(REQUEST_HEADER_PREFIX)) {
            var jwt = token.get().substring(REQUEST_HEADER_PREFIX.length());
            try {
                var claims = jwtParser.claims(jwt);
                String uuid = claims.get("uuid", String.class);
                String role = claims.get("role", String.class);
                var mutatedRequest = request.mutate().headers((headers) -> {
                    headers.put(REQUEST_HEADER_NAME, Collections.emptyList());
                    headers.put(USER_ID_HEADER_NAME, Collections.singletonList(uuid));
                    headers.put(USER_ROLE_HEADER_NAME, Collections.singletonList(role));
                }).build();
                var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange);

            } catch (ExpiredJwtException ignored) {
                var mutatedRequest = request.mutate().headers((headers) -> headers.put(REQUEST_HEADER_NAME, Collections.emptyList())).build();
                var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange);
            } catch (SignatureException e){
                log.warn("Invalid JWT token signature: {}", e.getMessage());
                return onError(exchange, "Invalid token signature");
            } catch (JwtException e){
                log.warn("Invalid JWT token: {}", e.getMessage());
                return onError(exchange, "Invalid access token");
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var errorResponse = new ErrorResponseDto(message, Instant.now(), 401);
        byte[] bytes = objectMapper.writeValueAsString(errorResponse).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
