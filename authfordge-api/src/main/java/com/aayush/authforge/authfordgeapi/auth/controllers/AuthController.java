package com.aayush.authforge.authfordgeapi.auth.controllers;

import com.aayush.authforge.authfordgeapi.auth.io.*;
import com.aayush.authforge.authfordgeapi.auth.otp.OtpService;
import com.aayush.authforge.authfordgeapi.auth.refresh.RefreshToken;
import com.aayush.authforge.authfordgeapi.auth.refresh.RefreshTokenService;
import com.aayush.authforge.authfordgeapi.auth.security.CookieService;
import com.aayush.authforge.authfordgeapi.auth.security.JwtService;
import com.aayush.authforge.authfordgeapi.auth.services.AuthService;
import com.aayush.authforge.authfordgeapi.common.exceptions.InvalidTokenException;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.mapper.UserMapper;
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
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;

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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerLocalUser(request);
        otpService.generateAndSendOtp(request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("User registered. OTP sent to email."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticate(request);
        User user = (User) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user);

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
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshCookie(refreshTokenService.rotateRefreshToken(token), jwtService.getRefreshTtlSeconds()).toString())
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

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = getRefreshCookie(request);
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
