package com.bookhub.authservice.services;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.enums.UserRole;

public interface AuthService {
    String authenticate(String email, String password);
    void registerNewUser(String email, String password, UserRole role, PersonDataRequestDto personDataRequestDto);
}
