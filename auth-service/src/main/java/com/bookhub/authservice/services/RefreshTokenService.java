package com.bookhub.authservice.services;

import com.bookhub.authservice.models.RefreshToken;
import org.springframework.security.core.Authentication;

public interface RefreshTokenService {
    RefreshToken generateToken(Authentication authentication);
}
