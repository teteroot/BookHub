package com.bookhub.authservice.services;

import com.bookhub.authservice.enums.UserRole;

public interface RegisterService {
    void register(String email, String password, UserRole role);
}
