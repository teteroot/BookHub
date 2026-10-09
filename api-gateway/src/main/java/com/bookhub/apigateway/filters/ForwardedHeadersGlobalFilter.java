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

    private static final String FORWARDED_HOST_HEADER_NAME = "X-Forwarded-Host";
    private static final String FORWARDED_PROTO_HEADER_NAME = "X-Forwarded-Proto";
    private static final String FORWARDED_PORT_HEADER_NAME = "X-Forwarded-Port";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(FORWARDED_HOST_HEADER_NAME, request.getURI().getHost())
                .header(FORWARDED_PROTO_HEADER_NAME, request.getURI().getScheme())
                .header(FORWARDED_PORT_HEADER_NAME, String.valueOf(request.getURI().getPort()))
                .build();
        var mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
