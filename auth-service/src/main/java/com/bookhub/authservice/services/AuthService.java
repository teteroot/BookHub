package com.bookhub.authservice.services;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.models.RefreshToken;
import org.springframework.security.core.Authentication;

public interface AuthService {
    Authentication authenticate(String email, String password);
    void registerNewUser(String email, String password, UserRole role, PersonDataRequestDto personDataRequestDto);
    String refreshAccessToken(String refreshToken);
    String generateAccessToken(Authentication authentication);
    RefreshToken generateRefreshToken(Authentication authentication);
}
