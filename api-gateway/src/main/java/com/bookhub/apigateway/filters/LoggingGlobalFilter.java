package com.bookhub.apigateway.filters;

import com.bookhub.apigateway.security.JwtParser;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private final String REQUEST_HEADER_NAME = "Authorization";
    private final String REQUEST_HEADER_PREFIX = "Bearer ";
    private final String USER_ID_HEADER_NAME = "X-User-Id";
    private final String USER_ROLE_HEADER_NAME = "X-User-Role";
    private final JwtParser jwtParser;

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

            } catch (JwtException ignored) {

            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
