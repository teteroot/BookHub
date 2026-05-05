package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.exceptions.extensions.RefreshTokenNotFoundException;
import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.models.User;
import com.bookhub.authservice.repositories.RefreshTokenRepository;
import com.bookhub.authservice.security.UserDetailsImpl;
import com.bookhub.authservice.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${security.refresh.lifetime}")
    private Long lifetime;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public RefreshToken generateToken(Authentication authentication) {
        if (authentication.getPrincipal() == null) throw new UserNotFoundException();
        var user = ((UserDetailsImpl)authentication.getPrincipal()).getUser();
        var token = RefreshToken.builder()
                .user(user)
                .expiration(Instant.now().plusMillis(lifetime))
                .build();
        refreshTokenRepository.save(token);
        return token;
    }

    @Override
    public RefreshToken loadUserRefreshToken(User user) {
        return refreshTokenRepository.findByUser(user)
                .orElseThrow(RefreshTokenNotFoundException::new);
    }

    @Override
    @Transactional
    public void updateExpiration(RefreshToken refreshToken) {
        refreshToken.setExpiration(Instant.now().plusMillis(lifetime));
        refreshTokenRepository.save(refreshToken);
    }
}
