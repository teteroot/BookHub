package com.bookhub.bookservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;

@Component
public class GatewayVerificationFilter extends OncePerRequestFilter {

    @Value("${security.origin.gateway.secret}")
    private String gatewaySecret;

    private final String REQUEST_HEADER_NAME = "X-Gateway-Secret";

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var secret = request.getHeader(REQUEST_HEADER_NAME);
        if (secret == null || !MessageDigest.isEqual(gatewaySecret.getBytes(), secret.getBytes())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
