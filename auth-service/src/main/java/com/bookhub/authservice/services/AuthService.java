package com.bookhub.authservice.services;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.enums.UserRole;
import org.springframework.security.core.Authentication;

public interface AuthService {
    Authentication authenticate(String email, String password);
    void registerNewUser(String email, String password, UserRole role, PersonDataRequestDto personDataRequestDto);

    String generateAccessToken(Authentication authentication);
    String generateRefreshToken(Authentication authentication);
}
