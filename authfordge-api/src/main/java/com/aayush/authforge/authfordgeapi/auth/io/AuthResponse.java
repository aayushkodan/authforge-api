package com.aayush.authforge.authfordgeapi.auth.io;

import com.aayush.authforge.authfordgeapi.user.io.UserResponse;
import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {}