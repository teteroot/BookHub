package com.bookhub.authservice.services;

import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.models.User;

import java.util.UUID;

public interface UserService {
    User loadUserByUUID(UUID uuid);

    void updateUserRole(UUID userId, UserRole userRole);
}
