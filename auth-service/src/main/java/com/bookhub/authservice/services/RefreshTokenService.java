package com.bookhub.authservice.services;

import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.models.User;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken generateToken(Authentication authentication);
    RefreshToken loadUserRefreshToken(User user);
    RefreshToken loadTokenByUUID(UUID token);
    void updateExpiration(RefreshToken refreshToken);
    void checkTokenExpiration(RefreshToken refreshToken);
}
