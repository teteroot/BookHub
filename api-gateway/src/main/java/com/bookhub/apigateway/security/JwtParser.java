package com.bookhub.apigateway.security;

import com.bookhub.apigateway.properties.SecurityOriginProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtParser {

    private final SecurityOriginProperties securityOriginProperties;

    public Claims claims(String jwt){
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(securityOriginProperties.getJwtKey().getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(jwt).getPayload();
    }
}
