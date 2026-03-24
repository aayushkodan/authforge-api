package com.aayush.authforge.authfordgeapi.auth.controllers;

import com.aayush.authforge.authfordgeapi.auth.io.*;
import com.aayush.authforge.authfordgeapi.auth.mappers.SessionMapper;
import com.aayush.authforge.authfordgeapi.auth.otp.OtpService;
import com.aayush.authforge.authfordgeapi.auth.refresh.RefreshToken;
import com.aayush.authforge.authfordgeapi.auth.refresh.RefreshTokenService;
import com.aayush.authforge.authfordgeapi.auth.security.CookieService;
import com.aayush.authforge.authfordgeapi.auth.security.JwtService;
import com.aayush.authforge.authfordgeapi.auth.services.AuthService;
import com.aayush.authforge.authfordgeapi.common.exceptions.InvalidTokenException;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.mapper.UserMapper;
import com.aayush.authforge.authfordgeapi.user.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final AuthService authService;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final CookieService cookieService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerLocalUser(request);
        try {
            otpService.generateAndSendOtp(request.email());
        } catch (Exception e) {
            // Log the actual error
            System.err.println("Failed to send OTP: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("User registered. OTP sent to email."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticate(request);
        User user = (User) authentication.getPrincipal();

        if (user.isTwoFactorEnabled()) {

            otpService.generateAndSendOtp(user.getEmail());

            return ResponseEntity.ok(new AuthResponse(
                    null,
                    "2fa_required",
                    0,
                    UserMapper.toResponse(user)
            ));
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user, httpRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshCookie(refreshToken, jwtService.getRefreshTtlSeconds()).toString())
                .body(new AuthResponse(accessToken, "access", 23, UserMapper.toResponse(user)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = getRefreshCookie(request);

        RefreshToken token = refreshTokenService.validateRefreshToken(refreshToken);

        User user = token.getUser();
        String accessToken = jwtService.generateAccessToken(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshCookie(refreshTokenService.rotateRefreshToken(token, request), jwtService.getRefreshTtlSeconds()).toString())
                .body(new AuthResponse(accessToken, "access", jwtService.getAccessTtlSeconds(), UserMapper.toResponse(user)));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        otpService.generateAndSendOtp(request.email());

        return ResponseEntity.ok(ApiResponse.of("OTP resent successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyEmailRequest request) {
        otpService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of("OTP verified successfully"));
    }

    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse> enable2fa(Authentication auth) {

        User user = (User) auth.getPrincipal();

        otpService.generateAndSendOtp(user.getEmail());

        return ResponseEntity.ok(
                ApiResponse.of("OTP sent to verify 2FA enable")
        );
    }

    @PostMapping("/2fa/verify-otp")
    public ResponseEntity<ApiResponse> verify2faOtp(Authentication authentication,@Valid @RequestBody VerifyEmailRequest request) {

        User user = (User) authentication.getPrincipal();
        otpService.verifyOtp(request.email(), request.otp());
        user.setEnabled(true);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of("OTP verified successfully"));
    }

    @PostMapping("/2fa/login")
    public ResponseEntity<AuthResponse> verifyLoginOtp(
            @RequestBody VerifyEmailRequest request,
            HttpServletRequest httpRequest
    ) {

        otpService.verifyOtp(request.email(), request.otp());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken =
                refreshTokenService.generateRefreshToken(user, httpRequest);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookieService.createRefreshCookie(
                                refreshToken,
                                jwtService.getRefreshTtlSeconds()
                        ).toString()
                )
                .body(
                        new AuthResponse(
                                accessToken,
                                "access",
                                jwtService.getAccessTtlSeconds(),
                                UserMapper.toResponse(user)
                        )
                );
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse> disable2fa(Authentication auth) {

        User user = (User) auth.getPrincipal();

        user.setTwoFactorEnabled(false);

        userRepository.save(user);

        return ResponseEntity.ok(
                ApiResponse.of("2FA disabled")
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getSessions(Authentication authentication, HttpServletRequest request) {

        User user = (User) authentication.getPrincipal();
        String refreshToken = getRefreshCookie(request);

        RefreshToken currentToken = refreshTokenService.validateRefreshToken(refreshToken);

        if (!currentToken.getUser().getId().equals(user.getId())) {
            throw new InvalidTokenException("Token does not belong to user");
        }

        List<RefreshToken> activeSessions = refreshTokenService.getActiveSessions(user);

        List<SessionResponse> sessions = activeSessions.stream().map(session -> SessionMapper.toResponse(session, currentToken.getId())).toList();

        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse> revokeSession(
            @PathVariable UUID id,
            Authentication authentication,
            HttpServletRequest request
    ) {

        User user = (User) authentication.getPrincipal();

        // 1️⃣ Identify current session
        String rawRefreshToken = getRefreshCookie(request);
        RefreshToken currentToken =
                refreshTokenService.validateRefreshToken(rawRefreshToken);

        // 2️⃣ Prevent revoking current session
        if (currentToken.getId().equals(id)) {
            throw new InvalidTokenException("Cannot revoke current session");
        }

        // 3️⃣ Revoke requested session
        refreshTokenService.revokeSession(user, id);

        return ResponseEntity.ok(
                ApiResponse.of("Session revoked successfully")
        );
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<ApiResponse> revokeAllOtherSessions(
            Authentication authentication,
            HttpServletRequest request
    ) {

        User user = (User) authentication.getPrincipal();

        // 1️⃣ Identify current session
        String rawRefreshToken = getRefreshCookie(request);
        RefreshToken currentToken =
                refreshTokenService.validateRefreshToken(rawRefreshToken);

        // 2️⃣ Security check
        if (!currentToken.getUser().getId().equals(user.getId())) {
            throw new InvalidTokenException("Token does not belong to user");
        }

        // 3️⃣ Revoke all other sessions
        refreshTokenService.revokeAllOtherSessions(user, currentToken.getId());

        return ResponseEntity.ok(
                ApiResponse.of("Logged out from all other devices")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {

        User user = (User) authentication.getPrincipal();
        String refreshToken = getRefreshCookie(request);
        RefreshToken token = refreshTokenService.validateRefreshToken(refreshToken);

        if (!token.getUser().getId().equals(user.getId())) {
            throw new InvalidTokenException("Token does not belong to user");
        }

        refreshTokenService.revokeByToken(refreshToken);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookieService.clearRefreshCookie().toString()
        );

        return ResponseEntity.ok(ApiResponse.of("Logged out successfully"));
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    private String getRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new InvalidTokenException("Refresh token missing");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(cookieService.getCookieName()))
                .findFirst().orElseThrow(() -> new InvalidTokenException("Refresh token not found")).getValue();
    }
}
