package com.bookhub.authservice.services;

import com.bookhub.authservice.enums.UserRole;

import java.util.UUID;

public interface RegisterService {
    UUID register(String email, String password, UserRole role);
    void rejectRegistration(UUID uuid);
}
