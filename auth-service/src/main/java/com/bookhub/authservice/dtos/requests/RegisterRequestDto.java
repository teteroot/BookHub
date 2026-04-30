package com.bookhub.authservice.dtos.requests;

import com.bookhub.authservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RegisterRequestDto {

    private String email;
    private String password;
    private UserRole role;
    private PersonDataRequestDto personData;
}
