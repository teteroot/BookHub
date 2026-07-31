package com.bookhub.bookservice.security;

import com.bookhub.bookservice.enums.UserRole;
import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class GatewayUserDetails implements UserDetails {
    @Getter
    private final UUID userId;

    private final UserRole role;

    public GatewayUserDetails(UUID userId, UserRole role) {
        this.userId = userId;
        this.role = role;
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_%s".formatted(role));
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    @NullMarked
    public  String getUsername() {
        return userId.toString();
    }
}
