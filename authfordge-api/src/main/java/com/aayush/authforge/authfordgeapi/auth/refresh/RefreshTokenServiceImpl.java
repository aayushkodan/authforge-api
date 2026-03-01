package com.aayush.authforge.authfordgeapi.auth.refresh;

import com.aayush.authforge.authfordgeapi.auth.security.JwtService;
import com.aayush.authforge.authfordgeapi.common.exceptions.InvalidTokenException;
import com.aayush.authforge.authfordgeapi.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Override
    public String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    public RefreshToken validateRefreshToken(String refreshToken) {
        String hashToken = hashToken(refreshToken);
        RefreshToken token= refreshTokenRepository.findByTokenHash(hashToken).orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if(token.getExpiresAt().isBefore(Instant.now())){
            throw new InvalidTokenException("Refresh token expired");
        }

        if(token.isRevoked()){
            throw new InvalidTokenException("Refresh token revoked");
        }
        return token;
    }

    @Override
    public String rotateRefreshToken(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        oldToken.setExpiresAt(Instant.now());
        refreshTokenRepository.save(oldToken);
        return generateRefreshToken(oldToken.getUser());
    }

    @Override
    public void revokeByToken(String rawToken) {
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed");
        }
    }
}
