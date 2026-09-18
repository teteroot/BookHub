package com.bookhub.apigateway.filters;

import com.bookhub.apigateway.properties.SecurityOriginProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@NullMarked
@RequiredArgsConstructor
@Component
public class CustomHeaderRemovalFilter extends GlobalGatewayFilter implements GlobalFilter, Ordered {

    private final SecurityOriginProperties securityOriginProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        var securedHeaders = securityOriginProperties.getSecuredHeaders();
        var mutatedRequest = exchange.getRequest().mutate().headers(
                (httpHeaders) -> {
                    if (securedHeaders != null) {
                        securedHeaders.forEach(httpHeaders::remove);
                    }
                }
        ).build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -3;
    }
}
