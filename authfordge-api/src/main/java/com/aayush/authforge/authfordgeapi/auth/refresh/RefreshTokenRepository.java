package com.aayush.authforge.authfordgeapi.auth.refresh;

import com.aayush.authforge.authfordgeapi.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserAndRevokedFalseAndExpiresAtAfter(User user, Instant expiresAtAfter);

    Optional<RefreshToken> findByIdAndUser(UUID id, User user);

    @Modifying
    @Query("""
    UPDATE RefreshToken r
    SET r.revoked = true
    WHERE r.user = :user
    AND r.id <> :currentId
    AND r.revoked = false
""")
    void revokeAllExceptCurrent(User user, UUID currentId);
}
