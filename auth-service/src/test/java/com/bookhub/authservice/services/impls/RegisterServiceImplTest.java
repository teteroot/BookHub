package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.exceptions.extensions.EmailIsAlreadyUsedException;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterServiceImpl registerService;

    @Test
    void testSuccessfulRegister() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        assertDoesNotThrow(() -> registerService.register("", "", UserRole.READER));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterWithEmailAlreadyUsed() {
        when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThrows(EmailIsAlreadyUsedException.class, () -> registerService.register("", "", UserRole.READER));
    }
}