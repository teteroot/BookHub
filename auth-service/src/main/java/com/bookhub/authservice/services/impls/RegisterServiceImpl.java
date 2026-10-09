package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.exceptions.extensions.EmailIsAlreadyUsedException;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.repositories.UserRepository;
import com.bookhub.authservice.services.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public UUID register(String email, String password, UserRole role) {
        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new EmailIsAlreadyUsedException(email);
        }
        var user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Objects.requireNonNullElse(role, UserRole.READER))
                .build();
        var savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Override
    @Transactional
    public void rejectRegistration(UUID uuid) {
        userRepository.deleteById(uuid);
    }

}
