package com.aayush.authforge.authfordgeapi.auth.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email
) {
}
