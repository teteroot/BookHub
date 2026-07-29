package com.bookhub.bookservice.security.filters;

import com.bookhub.bookservice.dtos.responses.ErrorResponseDto;
import com.bookhub.bookservice.enums.UserRole;
import com.bookhub.bookservice.security.GatewayUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class HeaderFilter extends OncePerRequestFilter {
    private static final String USER_ID_HEADER_NAME = "X-User-Id";
    private static final String USER_ROLE_HEADER_NAME = "X-User-Role";
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String roleHeader = request.getHeader(USER_ROLE_HEADER_NAME);
        String uuidHeader = request.getHeader(USER_ID_HEADER_NAME);
        if (uuidHeader == null || uuidHeader.isEmpty()) {
            filterChain.doFilter(request,response);
            return;
        }
        try {
            UUID id = UUID.fromString(uuidHeader);
            UserRole role = UserRole.valueOf(roleHeader);
            var userDetails = new GatewayUserDetails(id,role);
            var auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (IllegalArgumentException|NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8);
            var responseDto = new ErrorResponseDto("Invalid headers", Instant.now(),400);
            response.getWriter().write(objectMapper.writeValueAsString(responseDto));
            return;
        }
        filterChain.doFilter(request,response);
    }
}
