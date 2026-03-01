package com.aayush.authforge.authfordgeapi.auth.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieService {

    private final String cookieName;
    private final boolean cookieHttpOnly;
    private final boolean cookieSecure;
    private final String domain;
    private final String sameSite;

    public CookieService(
            @Value("${security.jwt.refresh-token-cookie-name}") String cookieName,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-domain}") String domain,
            @Value("${security.jwt.cookie-same-site}") String sameSite
    ) {
        this.cookieName = cookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.domain = domain;
        this.sameSite = sameSite;
    }

    public ResponseCookie createRefreshCookie(String token, long maxage) {
        return ResponseCookie.from(cookieName, token)
                .domain(domain)
                .sameSite(sameSite)
                .secure(cookieSecure)
                .httpOnly(cookieHttpOnly)
                .maxAge(maxage)
                .path("/")
                .build();
    }

    public ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(sameSite)
                .domain(domain)
                .path("/")
                .maxAge(0)
                .build();
    }
}
