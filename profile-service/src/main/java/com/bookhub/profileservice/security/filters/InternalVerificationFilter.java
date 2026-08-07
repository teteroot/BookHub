package com.bookhub.profileservice.security.filters;

import com.bookhub.profileservice.config.properties.SecurityOriginProperties;
import com.bookhub.profileservice.security.GatewayUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;

@RequiredArgsConstructor
@Component
public class InternalVerificationFilter extends OncePerRequestFilter {

    private final SecurityOriginProperties securityOriginProperties;

    private static final String INTERNAL_HEADER = "X-Internal-Secret";

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var secret = request.getHeader(INTERNAL_HEADER);
        if (secret == null || !MessageDigest.isEqual(securityOriginProperties.getInternalSecret().getBytes(), secret.getBytes())) {
            filterChain.doFilter(request, response);
            return;
        }

        var existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth == null || !(existingAuth.getPrincipal() instanceof GatewayUserDetails)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ArrayList<GrantedAuthority> authorities = new ArrayList<>(existingAuth.getAuthorities());
        authorities.add((GrantedAuthority) () -> "ROLE_INTERNAL");
        var newAuth = new UsernamePasswordAuthenticationToken(existingAuth.getPrincipal(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        filterChain.doFilter(request, response);
    }
}