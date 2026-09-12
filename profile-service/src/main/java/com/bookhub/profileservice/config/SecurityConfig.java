package com.bookhub.profileservice.config;

import com.bookhub.profileservice.security.filters.GatewayVerificationFilter;
import com.bookhub.profileservice.security.filters.HeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderFilter headerFilter;
    private final GatewayVerificationFilter gatewayVerificationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/profile/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/persons").fullyAuthenticated()
                        .requestMatchers("/api/v1/persons/me").fullyAuthenticated()
                        .requestMatchers("/api/v1/persons/favorites","/api/v1/persons/favorites/**").fullyAuthenticated()
                        .requestMatchers("/api/v1/persons/**").permitAll()
                        .anyRequest().fullyAuthenticated()

                ).addFilterBefore(gatewayVerificationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(headerFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

}
