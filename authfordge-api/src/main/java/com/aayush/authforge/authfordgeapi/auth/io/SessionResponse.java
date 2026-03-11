package com.aayush.authforge.authfordgeapi.auth.io;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String device,
        String ipAddress,
        String location,
        Instant createdAt,
        boolean current
) {}
