package com.aayush.authforge.authfordgeapi.user.services;

import java.util.UUID;

import com.aayush.authforge.authfordgeapi.user.io.ChangePasswordRequest;
import com.aayush.authforge.authfordgeapi.user.io.UpdateProfileRequest;
import com.aayush.authforge.authfordgeapi.user.io.UserResponse;

public interface UserService {

    UserResponse getByEmail(String email);

    UserResponse getById(UUID id);

    void enableUser(UUID userId);

    void disableUser(UUID userId);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    void deleteUser(String email);
}
