package com.bookhub.apigateway.filters;

import com.bookhub.apigateway.properties.SecurityOriginProperties;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class GatewayVerificationFilter implements GlobalFilter, Ordered {

    private final SecurityOriginProperties securityOriginProperties;
    private static final String REQUEST_HEADER_NAME = "X-Gateway-Secret";

    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest().mutate().header(REQUEST_HEADER_NAME, securityOriginProperties.getGatewaySecret()).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
