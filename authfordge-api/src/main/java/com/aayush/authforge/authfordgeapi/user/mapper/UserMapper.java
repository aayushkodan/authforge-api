package com.aayush.authforge.authfordgeapi.user.mapper;

import com.aayush.authforge.authfordgeapi.entities.Role;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.io.UserResponse;

import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper(){}

    public static UserResponse toResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .image(user.getProfilePicture())
                .enabled(user.isEnabled())
                .provider(user.getProvider())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
