package com.aayush.authforge.authfordgeapi.auth.controllers;

import com.aayush.authforge.authfordgeapi.auth.io.*;
import com.aayush.authforge.authfordgeapi.auth.otp.OtpService;
import com.aayush.authforge.authfordgeapi.auth.security.JwtService;
import com.aayush.authforge.authfordgeapi.auth.services.AuthService;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.io.UserResponse;
import com.aayush.authforge.authfordgeapi.user.mapper.UserMapper;
import com.aayush.authforge.authfordgeapi.user.repositories.UserRepository;
import com.aayush.authforge.authfordgeapi.user.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final AuthService authService;
    private final JwtService jwtService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerLocalUser(request);
        otpService.generateAndSendOtp(request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse.builder().message("User registered. OTP sent to email.").build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticate(request);
        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateAccessToken(user);

        return ResponseEntity.status(HttpStatus.OK).body(AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTtlSeconds())
                .user(UserMapper.toResponse(user))
                .build()
        );

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOtp(@Valid @RequestBody VerifyEmailRequest request) {
        if (otpService.verifyOtp(request.email(), request.otp())) {
            return ResponseEntity.ok().build();
        }
        else{
            return ResponseEntity.badRequest().build();
        }
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        } catch (Exception e) {
            System.out.println(loginRequest.email() + " wres " + loginRequest.password());
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
