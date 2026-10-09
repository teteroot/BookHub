package com.bookhub.apigateway.config;


import com.bookhub.apigateway.properties.SecurityOriginProperties;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Configuration
public class JwtConfig {

    private final SecurityOriginProperties securityOriginProperties;

    @Bean
    public JwtParser jwtParser(){
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(securityOriginProperties.getJwtKey().getBytes(StandardCharsets.UTF_8)))
                .build();
    }
}
