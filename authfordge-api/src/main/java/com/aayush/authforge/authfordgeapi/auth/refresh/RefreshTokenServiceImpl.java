package com.aayush.authforge.authfordgeapi.auth.refresh;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.aayush.authforge.authfordgeapi.auth.security.JwtService;
import com.aayush.authforge.authfordgeapi.common.exceptions.InvalidTokenException;
import com.aayush.authforge.authfordgeapi.common.location.LocationService;
import com.aayush.authforge.authfordgeapi.entities.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final LocationService locationService;

    @Override
    @Transactional
    public String generateRefreshToken(User user, HttpServletRequest request) {
        String device = request.getHeader("User-Agent");
        String ip = extractIp(request);
        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);
        String location = locationService.getLocation(ip);
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .device(device)
                .ipAddress(ip)
                .location(location)
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    @Override
    @Transactional
    public RefreshToken validateRefreshToken(String refreshToken) {
        String hashToken = hashToken(refreshToken);
        RefreshToken token= refreshTokenRepository.findByTokenHash(hashToken).orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if(token.getExpiresAt().isBefore(Instant.now())){
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new InvalidTokenException("Refresh token expired");
        }

        if(token.isRevoked()){
            throw new InvalidTokenException("Refresh token revoked");
        }
        return token;
    }

    @Override
    @Transactional
    public String rotateRefreshToken(RefreshToken oldToken,HttpServletRequest request) {
        oldToken.setRevoked(true);
        oldToken.setExpiresAt(Instant.now());
        refreshTokenRepository.save(oldToken);
        return generateRefreshToken(oldToken.getUser(),request);
    }

    @Override
    @Transactional
    public void revokeByToken(String rawToken) {
        String hash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public List<RefreshToken> getActiveSessions(User user) {
        return refreshTokenRepository.findAllByUserAndRevokedFalseAndExpiresAtAfter(user,Instant.now());
    }

    @Override
    @Transactional
    public void revokeSession(User user, UUID sessionId) {

        RefreshToken token = refreshTokenRepository
                .findByIdAndUser(sessionId, user)
                .orElseThrow(() ->
                        new InvalidTokenException("Session not found"));

        if (token.isRevoked()) {
            return; // already revoked, idempotent
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void revokeAllOtherSessions(User user, UUID currentSessionId) {
        refreshTokenRepository.revokeAllExceptCurrent(user, currentSessionId);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Token hashing failed", e);
        }
    }

private String extractIp(HttpServletRequest request) {

    String ip = request.getHeader("X-Forwarded-For");

    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("X-Real-IP");
    }

    if (ip == null || ip.isEmpty()) {
        ip = request.getRemoteAddr();
    }

    return ip.split(",")[0];
}
}
