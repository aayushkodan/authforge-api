package com.aayush.authforge.authfordgeapi.auth.io;

import lombok.Builder;

@Builder
public record ApiResponse(
        String message
) {
    public static ApiResponse of(String message) {
        return new ApiResponse(message);
    }
}
