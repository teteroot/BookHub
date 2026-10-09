package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testSuccessfulLoadUserByUUID() {
        var uuid = UUID.randomUUID();
        var user = new User();
        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));

        var result = assertDoesNotThrow(() -> userService.loadUserByUUID(uuid));
        assertEquals(user, result);
    }

    @Test
    void testLoadUserByUUIDNotFound() {
        var uuid = UUID.randomUUID();
        when(userRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.loadUserByUUID(uuid));
    }
}