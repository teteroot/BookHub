package com.bookhub.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtCore {

    private final JwtParser jwtParser;

    public Claims claims(String jwt){
        return jwtParser.parseSignedClaims(jwt).getPayload();
    }
}
