package com.bookhub.authservice.repositories;

import com.bookhub.authservice.models.RefreshToken;
import com.bookhub.authservice.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByUser(User user);

}
