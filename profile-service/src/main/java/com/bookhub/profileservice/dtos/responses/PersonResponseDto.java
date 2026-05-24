package com.bookhub.profileservice.dtos.responses;

import com.bookhub.profileservice.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

public record PersonResponseDto(UUID id,
                                String firstName,
                                String lastName,
                                Instant dateOfBirth,
                                Instant dateOfRegistration,
                                UserRole role) {
}
