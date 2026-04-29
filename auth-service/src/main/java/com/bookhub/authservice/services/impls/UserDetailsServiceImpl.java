package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.repositories.UserRepository;
import com.bookhub.authservice.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        var user = userRepository.findUserByEmail(username)
                        .orElseThrow(UserNotFoundException::new);
        return new UserDetailsImpl(user);
    }
}
