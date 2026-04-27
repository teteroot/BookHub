package com.bookhub.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@ConfigurationProperties(prefix = "token")
@Setter
public class JwtCore {

    private Integer lifetime;

    private String key;

    public String generateToken(Authentication authentication){
        return Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + lifetime))
                .signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }





}
