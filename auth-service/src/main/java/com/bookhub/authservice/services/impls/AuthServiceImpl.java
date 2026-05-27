package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenExpireException;
import com.bookhub.authservice.exceptions.extensions.RefreshTokenNotFoundException;
import com.bookhub.authservice.exceptions.extensions.UserNotFoundException;
import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.ports.ProfileProvisioningPort;
import com.bookhub.authservice.security.JwtCore;
import com.bookhub.authservice.security.UserDetailsImpl;
import com.bookhub.authservice.services.AuthService;
import com.bookhub.authservice.services.RefreshTokenService;
import com.bookhub.authservice.services.RegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final AuthenticationManager authenticationManager;
    private final RegisterService registerService;
    private final ProfileProvisioningPort profileProvisioningPort;
    private final JwtCore jwtCore;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Authentication authenticate(String email, String password) {
        var auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                email,
                password
        ));
        SecurityContextHolder.getContext().setAuthentication(auth);
        return auth;
    }

    @Override
    @Transactional
    public void registerNewUser(String email, String password, UserRole role, PersonDataRequestDto personDataRequestDto) {
        var uuid = registerService.register(email,password,role);
        personDataRequestDto.setId(uuid);
        personDataRequestDto.setRole(role);
        profileProvisioningPort.createPerson(personDataRequestDto);
    }

    @Override
    @Transactional(noRollbackFor = RefreshTokenExpireException.class)
    public String refreshAccessToken(String refreshToken) {
        try {
            var token = refreshTokenService.loadTokenByUUID(UUID.fromString(refreshToken));
            refreshTokenService.checkTokenExpiration(token);
            var userDetails = new UserDetailsImpl(token.getUser());
            var auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            return generateAccessToken(auth);
        } catch (IllegalArgumentException e){
            throw new RefreshTokenNotFoundException();
        }
    }


    @Override
    public String generateAccessToken(Authentication authentication) {
        return jwtCore.generateToken(authentication);
    }

    @Override
    public RefreshToken generateRefreshToken(Authentication authentication) {
        if (authentication.getPrincipal() == null) throw new UserNotFoundException();
        var user = ((UserDetailsImpl)authentication.getPrincipal()).getUser();
        try {
            var token = refreshTokenService.loadUserRefreshToken(user);
            refreshTokenService.updateExpiration(token);
            return token;
        } catch (RefreshTokenNotFoundException e){
            return refreshTokenService.generateToken(authentication);
        }
    }

}
