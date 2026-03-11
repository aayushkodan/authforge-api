package com.aayush.authforge.authfordgeapi.auth.mappers;

import com.aayush.authforge.authfordgeapi.auth.io.SessionResponse;
import com.aayush.authforge.authfordgeapi.auth.refresh.RefreshToken;

import java.util.UUID;

public class SessionMapper {

    public static SessionResponse toResponse(
            RefreshToken token,
            UUID currentTokenId
    ) {
        return new SessionResponse(
                token.getId(),
                token.getDevice(),
                token.getIpAddress(),
                token.getLocation(),
                token.getCreatedAt(),
                token.getId().equals(currentTokenId)
        );
    }
}
