package com.bookhub.profileservice.security;

import com.bookhub.profileservice.enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class HeaderFilter extends OncePerRequestFilter {
    private final String USER_ID_HEADER_NAME = "X-User-Id";
    private final String USER_ROLE_HEADER_NAME = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String roleHeader = request.getHeader(USER_ROLE_HEADER_NAME);
        String uuidHeader = request.getHeader(USER_ID_HEADER_NAME);
        if (uuidHeader == null || uuidHeader.isEmpty()) {
            filterChain.doFilter(request,response);
            return;
        }
        UUID id = UUID.fromString(uuidHeader);
        UserRole role = UserRole.valueOf(roleHeader);
        var userDetails = new GatewayUserDetails(id,role);
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request,response);
    }
}
