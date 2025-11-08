package com.example.javaddit.features.user.repository;

import com.example.javaddit.features.user.entity.RefreshToken;
import com.example.javaddit.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    long deleteByUserAndExpiresAtBefore(User user, Instant cutoff);
}
