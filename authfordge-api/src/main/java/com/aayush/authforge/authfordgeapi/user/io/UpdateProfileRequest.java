package com.aayush.authforge.authfordgeapi.user.io;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UpdateProfileRequest(

        @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
        String name,

        @URL(message = "Image must be a valid URL")
        @Pattern(
                regexp = "^https?://.*\\.(jpg|jpeg|png|gif|webp)$",
                message = "Image must be a valid image URL"
        )
        String profilePicture
) {}