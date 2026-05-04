package com.bookhub.authservice.controllers;

import com.bookhub.authservice.dtos.requests.LoginRequestDto;
import com.bookhub.authservice.dtos.requests.RegisterRequestDto;
import com.bookhub.authservice.dtos.responses.JwtResponseDto;
import com.bookhub.authservice.exceptions.extensions.IncorrectRegisterDataException;
import com.bookhub.authservice.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto registerRequestDto,
                                         BindingResult result){
        if (result.hasErrors()) throw new IncorrectRegisterDataException(result);
        authService.registerNewUser(
                registerRequestDto.getEmail(),
                registerRequestDto.getPassword(),
                registerRequestDto.getRole(),
                registerRequestDto.getPersonData()
        );
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        var auth = authService.authenticate(loginRequestDto.email(),loginRequestDto.password());
        return ResponseEntity.ok(new JwtResponseDto(
                authService.generateAccessToken(auth)
        ));
    }
}
