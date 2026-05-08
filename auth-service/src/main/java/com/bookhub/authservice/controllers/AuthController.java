package com.bookhub.authservice.controllers;

import com.bookhub.authservice.dtos.requests.LoginRequestDto;
import com.bookhub.authservice.dtos.requests.RegisterRequestDto;
import com.bookhub.authservice.dtos.responses.JwtResponseDto;
import com.bookhub.authservice.exceptions.TooManyRequestsException;
import com.bookhub.authservice.exceptions.extensions.IncorrectRegisterDataException;
import com.bookhub.authservice.services.AuthService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;
    private final Bucket bucket;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto registerRequestDto,
                                         BindingResult result){
        if (!bucket.tryConsume(1)) throw new TooManyRequestsException();
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
    public ResponseEntity<JwtResponseDto> login(@RequestBody LoginRequestDto loginRequestDto,
                                                HttpServletResponse response){
        if (!bucket.tryConsume(1)) throw new TooManyRequestsException();
        var auth = authService.authenticate(loginRequestDto.email(),loginRequestDto.password());
        var refreshToken = authService.generateRefreshToken(auth);
        var cookie = new Cookie("refreshToken", String.valueOf(refreshToken.getToken()));
        cookie.setMaxAge((int) Duration.between(Instant.now(), refreshToken.getExpiration()).getSeconds());
        cookie.setPath("/");
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.ok(new JwtResponseDto(
                authService.generateAccessToken(auth)
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refreshToken(@CookieValue(name = "refreshToken") String refreshToken){
        if (!bucket.tryConsume(1)) throw new TooManyRequestsException();
        var jwt = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new JwtResponseDto(jwt));
    }
}
