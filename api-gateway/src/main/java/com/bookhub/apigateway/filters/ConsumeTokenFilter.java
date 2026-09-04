package com.bookhub.apigateway.filters;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class ConsumeTokenFilter extends GlobalGatewayFilter implements GlobalFilter, Ordered {

    private final Bucket bucket;
    private final ObjectMapper objectMapper;

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!bucket.tryConsume(1)){
            return onError(exchange,"Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS, objectMapper);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
