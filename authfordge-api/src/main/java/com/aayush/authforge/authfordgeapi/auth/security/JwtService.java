package com.aayush.authforge.authfordgeapi.auth.security;

import com.aayush.authforge.authfordgeapi.common.exceptions.InvalidTokenException;
import com.aayush.authforge.authfordgeapi.entities.Role;
import com.aayush.authforge.authfordgeapi.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Getter
public class JwtService {

    private final SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public String generateAccessToken(User user) {

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(accessTtlSeconds)))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("token_type", "access")
                .signWith(key)
                .compact();
    }


    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token expired");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public void validateAccessToken(String token) {

        Claims claims = extractAllClaims(token);

        if (!issuer.equals(claims.getIssuer())) {
            throw new InvalidTokenException("Invalid issuer");
        }

        if (!"access".equals(claims.get("token_type"))) {
            throw new InvalidTokenException("Invalid token type");
        }
    }
}