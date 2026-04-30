package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.repositories.UserRepository;
import com.bookhub.authservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User loadUserByUUID(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(UserNotFoundException::new);
    }
}
