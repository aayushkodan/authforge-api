package com.aayush.authforge.authfordgeapi.auth.security;

import com.aayush.authforge.authfordgeapi.auth.io.AuthResponse;
import com.aayush.authforge.authfordgeapi.auth.refresh.RefreshTokenService;
import com.aayush.authforge.authfordgeapi.auth.services.OAuth2Service;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.mapper.UserMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final OAuth2Service oAuth2Service;
    private final ObjectMapper objectMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        User user = oAuth2Service.processOAuth2User(registrationId,oAuth2User);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(user);

        response.setHeader(HttpHeaders.SET_COOKIE, cookieService.createRefreshCookie(refreshToken, jwtService.getRefreshTtlSeconds()).toString());

        AuthResponse authResponse = new AuthResponse(accessToken, "access", 23, UserMapper.toResponse(user));
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(response.getWriter(), authResponse);


    }
}
