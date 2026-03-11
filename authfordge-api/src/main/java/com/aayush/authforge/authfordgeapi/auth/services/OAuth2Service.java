package com.aayush.authforge.authfordgeapi.auth.services;

import com.aayush.authforge.authfordgeapi.auth.oauth.OAuthUserInfo;
import com.aayush.authforge.authfordgeapi.auth.oauth.OAuthUserInfoFactory;
import com.aayush.authforge.authfordgeapi.entities.Provider;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.aayush.authforge.authfordgeapi.common.exceptions.RoleNotFoundException;
import com.aayush.authforge.authfordgeapi.entities.Role;
import com.aayush.authforge.authfordgeapi.role.repositories.RoleRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User processOAuth2User(String registrationId, OAuth2User oauth2User) {

        OAuthUserInfo userInfo = OAuthUserInfoFactory.getOAuthUserInfo(registrationId, oauth2User.getAttributes());

        return userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> registerOAuthUser(registrationId,userInfo));
    }

    private User registerOAuthUser(String registrationId, OAuthUserInfo info) {

                Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Default role not found"));

        User user = User.builder()
                .email(info.getEmail())
                .name(info.getName())
                .profilePicture(info.getImage())
                .provider(Provider.valueOf(registrationId.toUpperCase()))
                .enabled(true)
                .roles(Set.of(defaultRole))
                .build();

        return userRepository.save(user);
    }
}
