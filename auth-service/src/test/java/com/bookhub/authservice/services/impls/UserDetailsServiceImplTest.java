package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.repositories.UserRepository;
import com.bookhub.authservice.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void testSuccessfulLoadUserByUsername() {
        var user = new User();
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(user));

        var result = assertDoesNotThrow(() -> userDetailsService.loadUserByUsername("username"));
        assertNotNull(result);
        assertTrue(result instanceof UserDetailsImpl);
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userDetailsService.loadUserByUsername("username"));
    }
}