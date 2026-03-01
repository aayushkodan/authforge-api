package com.aayush.authforge.authfordgeapi.auth.oauth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

public class OAuthUserInfoFactory {

    public static OAuthUserInfo getOAuthUserInfo(
            String registrationId,
            Map<String, Object> attributes
    ) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuthUserInfo(attributes);
            case "github" -> new GithubOAuthUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported OAuth provider");
        };
    }
}
