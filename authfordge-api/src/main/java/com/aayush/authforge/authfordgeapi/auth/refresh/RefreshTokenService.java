package com.aayush.authforge.authfordgeapi.auth.refresh;

import com.aayush.authforge.authfordgeapi.entities.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface RefreshTokenService {

    String generateRefreshToken(User user, HttpServletRequest request);

    RefreshToken validateRefreshToken(String refreshToken);

    String rotateRefreshToken(RefreshToken refreshToken,HttpServletRequest request);

    void revokeByToken(String rawToken);

    List<RefreshToken> getActiveSessions(User user);

    void revokeSession(User user, UUID sessionId);

    void revokeAllOtherSessions(User user, UUID currentSessionId);
}
