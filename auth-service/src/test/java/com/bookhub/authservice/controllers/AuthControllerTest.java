package com.bookhub.authservice.controllers;

import com.bookhub.authservice.config.SecurityConfig;
import com.bookhub.authservice.dtos.requests.LoginRequestDto;
import com.bookhub.authservice.dtos.requests.RegisterRequestDto;
import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.exceptions.extensions.EmailIsAlreadyUsedException;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenExpireException;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenNotFoundException;
import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.security.JwtCore;
import com.bookhub.authservice.security.TokenFilter;
import com.bookhub.authservice.services.AuthService;
import com.bookhub.authservice.services.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtCore.class, TokenFilter.class})
class AuthControllerTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterWithIncorrectEmail() throws Exception {
        RegisterRequestDto registerRequestDTO = new RegisterRequestDto("t", "12345678", UserRole.AUTHOR, null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("incorrect email; "));
    }

    @Test
    void testRegisterWithIncorrectPassword() throws Exception {
        RegisterRequestDto registerRequestDTO = new RegisterRequestDto("test@test.com", "1", UserRole.AUTHOR, null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("password at least 6 characters; "));
    }

    @Test
    void testRegisterWithAlreadyUsedEmail() throws Exception {
        RegisterRequestDto registerRequestDTO = new RegisterRequestDto("test@test.com", "12345678", UserRole.AUTHOR, null);
        doThrow(new EmailIsAlreadyUsedException(registerRequestDTO.getEmail()))
                .when(authService).registerNewUser(eq("test@test.com"), any(), any(), any());
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email test@test.com is already in use"));
    }

    @Test
    void testSuccessfulRegister() throws Exception {
        RegisterRequestDto registerRequestDTO = new RegisterRequestDto("test@test.com", "12345678", UserRole.AUTHOR, null);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestDTO)))
                .andExpect(status().isCreated());
        verify(authService, Mockito.times(1)).registerNewUser(any(), any(), any(), any());
    }

    @Test
    void testLoginWithBadCredentials() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@test.com","incorrect");
        when(authService.authenticate(any(), any())).thenThrow(BadCredentialsException.class);
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect password"));

    }

    @Test
    void testSuccessfulLogin() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@test.com","password");
        UUID tokenUUID = UUID.randomUUID();
        when(authService.generateRefreshToken(any()))
                .thenReturn(
                        RefreshToken.builder()
                                .token(tokenUUID)
                                .expiration(Instant.now())
                                .build()
        );
        when(authService.generateAccessToken(any()))
                .thenReturn("accessToken");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", tokenUUID.toString()))
                .andExpect(jsonPath("$.accessToken").value("accessToken"));
    }

    @Test
    void testRefreshTokenWithoutCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testRefreshTokenWithoutCorrectToken() throws Exception {
        when(authService.refreshAccessToken(any())).thenThrow(new RefreshTokenNotFoundException());
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refreshToken", "invalid-token")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Refresh token not found"));
    }

    @Test
    void testRefreshTokenWithExpiredToken() throws Exception {
        when(authService.refreshAccessToken(any())).thenThrow(new RefreshTokenExpireException());
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refreshToken", "expired-token")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token is expire. Please re-login."));
    }
    @Test
    void testSuccessfulRefreshToken() throws Exception {
        when(authService.refreshAccessToken(any())).thenReturn("newAccessToken");
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("refreshToken", "correct-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"));
    }
}