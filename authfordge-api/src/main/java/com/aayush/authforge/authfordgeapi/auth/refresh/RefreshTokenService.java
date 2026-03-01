package com.aayush.authforge.authfordgeapi.auth.refresh;

import com.aayush.authforge.authfordgeapi.entities.User;

public interface RefreshTokenService {

    String generateRefreshToken(User user);

    RefreshToken validateRefreshToken(String refreshToken);

    String rotateRefreshToken(RefreshToken refreshToken);

    void revokeByToken(String rawToken);
}
