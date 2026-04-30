package com.bookhub.authservice.services;

import com.bookhub.authservice.models.User;

import java.util.UUID;

public interface UserService {
    User loadUserByUUID(UUID uuid);
}
