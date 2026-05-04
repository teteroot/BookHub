package com.bookhub.authservice.services.impls;

import com.bookhub.authservice.dtos.requests.PersonDataRequestDto;
import com.bookhub.authservice.enums.UserRole;
import com.bookhub.authservice.ports.ProfileProvisioningPort;
import com.bookhub.authservice.security.JwtCore;
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
    public void registerNewUser(String email, String password, UserRole role, PersonDataRequestDto personDataRequestDto) {
        registerService.register(email,password,role);
        profileProvisioningPort.createPerson(personDataRequestDto);
    }

    @Override
    public String generateAccessToken(Authentication authentication) {
        return jwtCore.generateToken(authentication);
    }

    @Override
    public String generateRefreshToken(Authentication authentication) {
        //TODO check exist token
        return String.valueOf(refreshTokenService.generateToken(authentication).getToken());
    }

}
