package com.bookhub.profileservice.security;

import com.bookhub.profileservice.config.properties.SecurityOriginProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class GatewayVerificationFilter extends OncePerRequestFilter {

    private final SecurityOriginProperties securityOriginProperties;

    private static final String REQUEST_HEADER_NAME = "X-Gateway-Secret";

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var secret = request.getHeader(REQUEST_HEADER_NAME);
        if (secret == null || !secret.equals(securityOriginProperties.getGatewaySecret())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
