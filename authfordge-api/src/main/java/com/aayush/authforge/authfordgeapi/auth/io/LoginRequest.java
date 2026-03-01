package com.aayush.authforge.authfordgeapi.auth.io;

import jakarta.validation.constraints.*;

public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=\\S+$).+$",  // Added (?=\\S+$)
                message = "Password must contain uppercase, lowercase, digit, and no whitespace"
        )
        String password
) {}