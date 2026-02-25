package com.aayush.authforge.authfordgeapi.auth.io;

public record VerifyEmailRequest(
        String email,
        String otp
) {
}
