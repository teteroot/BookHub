package com.bookhub.authservice.services;

import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.models.User;
import org.springframework.security.core.Authentication;

public interface RefreshTokenService {
    RefreshToken generateToken(Authentication authentication);
    RefreshToken loadUserRefreshToken(User user);
    void updateExpiration(RefreshToken refreshToken);
}
