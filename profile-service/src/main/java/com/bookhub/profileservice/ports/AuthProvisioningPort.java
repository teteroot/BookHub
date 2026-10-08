package com.bookhub.profileservice.ports;

import com.bookhub.profileservice.enums.UserRole;

public interface AuthProvisioningPort {

    void updateUserRole(String userId, String currentRole, UserRole newRole);
}
