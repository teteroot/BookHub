package com.bookhub.authservice.security;

import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class JwtCoreTest {

    @InjectMocks
    private JwtCore jwtCore;


    @Test
    void testGenerateToken() {
        ReflectionTestUtils.setField(jwtCore, "lifetime", 100);
        ReflectionTestUtils.setField(jwtCore, "key", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertDoesNotThrow(() -> jwtCore.generateToken(
                new UsernamePasswordAuthenticationToken(
                        new UserDetailsImpl(
                                User.builder()
                                        .id(UUID.randomUUID())
                                        .role(UserRole.AUTHOR)
                                        .build()
                        ), null))
        );
    }

    @Test
    void testGenerateTokenWithoutAuthentication() {
        assertThrows(UserNotFoundException.class, () -> jwtCore.generateToken(
                new UsernamePasswordAuthenticationToken(null, null))
        );
    }
}