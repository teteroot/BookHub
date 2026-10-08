package com.bookhub.authservice.dtos.requests;

import com.bookhub.authservice.enums.UserRole;

public record UserRolePatchRequestDto(UserRole role) {

}
