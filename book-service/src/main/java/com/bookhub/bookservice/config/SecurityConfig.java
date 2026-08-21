package com.bookhub.bookservice.config;

import com.bookhub.bookservice.security.filters.GatewayVerificationFilter;
import com.bookhub.bookservice.security.filters.HeaderFilter;
import com.bookhub.bookservice.security.filters.InternalVerificationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderFilter headerFilter;
    private final GatewayVerificationFilter gatewayVerificationFilter;
    private final InternalVerificationFilter internalVerificationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/api/v1/books").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/books/*/pages/*").fullyAuthenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/*").fullyAuthenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/books/**").fullyAuthenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/books/**", "/api/v1/books").fullyAuthenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/*/pages/*").permitAll()
                        .anyRequest().fullyAuthenticated()

                ).addFilterBefore(internalVerificationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(headerFilter, InternalVerificationFilter.class)
                .addFilterBefore(gatewayVerificationFilter, HeaderFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

}
