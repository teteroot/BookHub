package com.bookhub.authservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Instant expiration;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken token1 = (RefreshToken) o;
        return Objects.equals(token, token1.token);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token);
    }
}
