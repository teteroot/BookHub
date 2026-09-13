package com.bookhub.apigateway.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@NullMarked
@Component
@RequiredArgsConstructor
public class ForwardedHeadersGlobalFilter extends GlobalGatewayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Forwarded-Host", request.getURI().getHost())
                .header("X-Forwarded-Proto", request.getURI().getScheme())
                .header("X-Forwarded-Port", String.valueOf(request.getURI().getPort()))
                .build();
        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
