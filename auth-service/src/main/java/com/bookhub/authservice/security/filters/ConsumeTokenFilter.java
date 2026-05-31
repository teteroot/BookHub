package com.bookhub.authservice.security.filters;

import com.bookhub.authservice.dtos.responses.ErrorResponseDto;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@RequiredArgsConstructor
@Component
public class ConsumeTokenFilter extends OncePerRequestFilter {

    private final Bucket bucket;
    private final ObjectMapper objectMapper;

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!bucket.tryConsume(1)){
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8);
            var responseDto = new ErrorResponseDto("Too many requests. Please try again later.", Instant.now(),429);
            response.getWriter().write(objectMapper.writeValueAsString(responseDto));
            return;
        }
        filterChain.doFilter(request,response);
    }

}
