package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenExpireException;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenNotFoundException;
import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.ports.ProfileProvisioningPort;
import com.bookhub.authservice.security.JwtCore;
import com.bookhub.authservice.security.UserDetailsImpl;
import com.bookhub.authservice.services.RefreshTokenService;
import com.bookhub.authservice.services.RegisterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
        UUID tokenUUID = UUID.randomUUID();
        when(refreshTokenService.loadTokenByUUID(tokenUUID))
                .thenReturn(new RefreshToken(tokenUUID, new User(), Instant.now()));
        assertDoesNotThrow(() -> authService.refreshAccessToken(tokenUUID.toString()));
        verify(jwtCore, times(1)).generateToken(any(Authentication.class));
    }

    @Test
    void testRefreshExpiredAccessToken() {
        doThrow(RefreshTokenExpireException.class)
                .when(refreshTokenService).checkTokenExpiration(any());
        assertThrows(RefreshTokenExpireException.class,
                () -> authService.refreshAccessToken(UUID.randomUUID().toString()));
    }
    @Test
    void testRefreshAccessTokenWithNonExistRefresh() {
        when(refreshTokenService.loadTokenByUUID(any(UUID.class)))
                .thenThrow(RefreshTokenNotFoundException.class);
        assertThrows(RefreshTokenNotFoundException.class,
                () -> authService.refreshAccessToken(UUID.randomUUID().toString()));
    }

    @Test
    void testSuccessfulGenerateAccessToken() {
        assertDoesNotThrow(() -> authService.generateAccessToken(any(Authentication.class)));
        verify(jwtCore,times(1)).generateToken(any());
    }

    @Test
    void testSuccessfulGenerateRefreshTokenWithExistToken() {
        when(refreshTokenService.loadUserRefreshToken(any(User.class)))
                .thenThrow(RefreshTokenNotFoundException.class);
        RefreshToken refreshToken = new RefreshToken();
        when(refreshTokenService.generateToken(any(Authentication.class)))
                .thenReturn(refreshToken);
        assertEquals(refreshToken,authService.generateRefreshToken(
                new UsernamePasswordAuthenticationToken(
                        new UserDetailsImpl(new User()),
                        null,null
                )
        ));
    }

    @Test
    void testSuccessfulGenerateRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        when(refreshTokenService.loadUserRefreshToken(any(User.class)))
                .thenReturn(refreshToken);
        assertEquals(refreshToken,authService.generateRefreshToken(
                new UsernamePasswordAuthenticationToken(
                        new UserDetailsImpl(new User()),
                        null,null
                )
        ));
        verify(refreshTokenService,times(1)).updateExpiration(refreshToken);
    }

    @Test
    void testGenerateRefreshTokenForNonExistUser() {
        assertThrows(UserNotFoundException.class,() -> authService.generateRefreshToken(
                new UsernamePasswordAuthenticationToken(null,null,null)
        ));
    }
}