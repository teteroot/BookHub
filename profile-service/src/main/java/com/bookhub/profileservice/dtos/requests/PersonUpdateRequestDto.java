package com.bookhub.profileservice.dtos.requests;

import com.bookhub.profileservice.enums.UserRole;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonUpdateRequestDto {
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    private String firstName;

    @Size(min = 2, max = 20, message = "Last name must be between 2 and 20 characters")
    private String lastName;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    private Instant dateOfBirth;

    @Size(max = 4000, message = "Biography must not exceed 4000 characters")
    private String biography;

    private UserRole role;
}
