package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.exceptions.extensions.RefreshTokenExpireException;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenNotFoundException;
import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.repositories.RefreshTokenRepository;
import com.bookhub.authservice.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void testSuccessfulGenerateToken() {
        ReflectionTestUtils.setField(refreshTokenService, "lifetime", 3600000L);
        var user = new User();
        var userDetails = new UserDetailsImpl(user);
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());
        var result = assertDoesNotThrow(() -> refreshTokenService.generateToken(authentication));
        assertNotNull(result);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testGenerateTokenWithNullPrincipal() {
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> refreshTokenService.generateToken(authentication));
    }

    @Test
    void testSuccessfulLoadUserRefreshToken() {
        var user = new User();
        var refreshToken = new RefreshToken();
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(refreshToken));
        var result = assertDoesNotThrow(() -> refreshTokenService.loadUserRefreshToken(user));
        assertEquals(refreshToken, result);
    }

    @Test
    void testLoadUserRefreshTokenNotFound() {
        var user = new User();
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        assertThrows(RefreshTokenNotFoundException.class, () -> refreshTokenService.loadUserRefreshToken(user));
    }

    @Test
    void testSuccessfulLoadTokenByUUID() {
        var uuid = UUID.randomUUID();
        var refreshToken = new RefreshToken();
        when(refreshTokenRepository.findById(uuid)).thenReturn(Optional.of(refreshToken));
        var result = assertDoesNotThrow(() -> refreshTokenService.loadTokenByUUID(uuid));
        assertEquals(refreshToken, result);
    }

    @Test
    void testLoadTokenByUUIDNotFound() {
        var uuid = UUID.randomUUID();
        when(refreshTokenRepository.findById(uuid)).thenReturn(Optional.empty());
        assertThrows(RefreshTokenNotFoundException.class, () -> refreshTokenService.loadTokenByUUID(uuid));
    }

    @Test
    void testSuccessfulUpdateExpiration() {
        ReflectionTestUtils.setField(refreshTokenService, "lifetime", 3600000L);
        var refreshToken = new RefreshToken();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        assertDoesNotThrow(() -> refreshTokenService.updateExpiration(refreshToken));
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void testSuccessfulCheckTokenExpiration() {
        var refreshToken = new RefreshToken();
        refreshToken.setExpiration(Instant.now().plusMillis(1000));
        assertDoesNotThrow(() -> refreshTokenService.checkTokenExpiration(refreshToken));
    }

    @Test
    void testCheckTokenExpirationExpired() {
        var refreshToken = new RefreshToken();
        refreshToken.setExpiration(Instant.now().minusMillis(1000));
        assertThrows(RefreshTokenExpireException.class, () -> refreshTokenService.checkTokenExpiration(refreshToken));
    }
}