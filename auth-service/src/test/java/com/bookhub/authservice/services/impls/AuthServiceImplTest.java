package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenExpireException;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenNotFoundException;
import com.bookhub.authservice.ports.ProfileProvisioningPort;
import com.bookhub.authservice.security.JwtCore;
import com.bookhub.authservice.services.RefreshTokenService;
import com.bookhub.authservice.services.RegisterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RegisterService registerService;

    @Mock
    private ProfileProvisioningPort profileProvisioningPort;

    @Mock
    private JwtCore jwtCore;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testSuccessfulAuthentication() {
        assertDoesNotThrow(() -> authService.authenticate("",""));
        verify(authenticationManager,times(1)).authenticate(any(Authentication.class));
    }

    @Test
    void testAuthenticationWithBadCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException(""));
        assertThrows(BadCredentialsException.class, () -> authService.authenticate("",""));
    }


    @Test
    void testSuccessfulRegisterNewUser() {
        assertDoesNotThrow(
                () -> authService.registerNewUser("","",UserRole.READER,new PersonDataRequestDto())
        );
        verify(registerService,times(1)).register(anyString(),anyString(),any(UserRole.class));
        verify(profileProvisioningPort,times(1)).createPerson(any(PersonDataRequestDto.class));
    }



    @Test
    void testSuccessfulRefreshAccessToken() {

    }

    @Test
    void testRefreshExpiredAccessToken() {
        doThrow(RefreshTokenExpireException.class)
                .when(refreshTokenService).checkTokenExpiration(any());
        assertThrows(RefreshTokenExpireException.class,
                () -> authService.refreshAccessToken(UUID.randomUUID().toString()));
    }
    @Test
    void testRefreshNonExistAccessToken() {
        when(refreshTokenService.loadTokenByUUID(any(UUID.class)))
                .thenThrow(RefreshTokenNotFoundException.class);
        assertThrows(RefreshTokenNotFoundException.class,
                () -> authService.refreshAccessToken(UUID.randomUUID().toString()));
    }

    @Test
    void generateAccessToken() {
    }

    @Test
    void generateRefreshToken() {
    }
}