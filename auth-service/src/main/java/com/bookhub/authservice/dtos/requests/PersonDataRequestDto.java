package com.bookhub.authservice.dtos.requests;

import com.bookhub.authservice.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class PersonDataRequestDto {

    private String firstName;

    private String lastName;

    private Instant dateOfBirth;

    private String biography;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserRole role;

}
