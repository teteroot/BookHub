package com.bookhub.authservice.repositories;

import com.bookhub.authservice.models.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {
}
