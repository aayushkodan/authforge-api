package com.aayush.authforge.authfordgeapi.user.io;

import com.aayush.authforge.authfordgeapi.entities.Provider;
import lombok.Builder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String name,
        String email,
        String image,
        boolean enabled,
        Provider provider,
        Set<String> roles,
        Instant createdAt
) {}