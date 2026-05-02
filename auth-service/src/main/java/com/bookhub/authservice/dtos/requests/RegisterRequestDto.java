package com.bookhub.authservice.dtos.requests;

import com.bookhub.authservice.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {

    @Email(message = "incorrect email")
    private String email;

    @Size(min = 6, message = "password must be greater than 6")
    private String password;

    private UserRole role;

    private PersonDataRequestDto personData;

}
