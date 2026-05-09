package com.bookhub.profileservice.dtos.requests;

import com.bookhub.profileservice.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonCreateRequestDto {

    @NotNull(message = "ID must not be null")
    private UUID id;

    @NotBlank(message = "First name must not be empty")
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    private String firstName;

    @NotBlank(message = "Last name must not be empty")
    @Size(min = 2, max = 20, message = "Last name must be between 2 and 20 characters")
    private String lastName;

    @NotNull(message = "Date of birth must not be null")
    @PastOrPresent(message = "Date of birth must be in the past or present")
    private Instant dateOfBirth;

    @Size(max = 4000, message = "Biography must not exceed 4000 characters")
    private String biography;

    private UserRole role;
}
