package com.aayush.authforge.authfordgeapi.auth.security;

import com.aayush.authforge.authfordgeapi.common.exceptions.InvalidTokenException;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.repositories.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {

            jwtService.validateAccessToken(token);

            UUID userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                throw new InvalidTokenException("User not found");
            }

            if (!user.isEnabled()) {
                throw new InvalidTokenException("Account not verified");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (InvalidTokenException e) {
            request.setAttribute("error", e.getMessage());
        } catch (ExpiredJwtException e) {
            request.setAttribute("error", "Token expired");
        } catch (Exception e) {
            request.setAttribute("error", "Authentication failed");
        }

        filterChain.doFilter(request, response);
    }
}
