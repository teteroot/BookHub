package com.bookhub.authservice.dtos.requests;

import com.bookhub.authservice.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Size(min = 6, message = "password at least 6 characters")
    @NotBlank
    private String password;

    private UserRole role;

    @NotNull
    private PersonDataRequestDto personData;

}
