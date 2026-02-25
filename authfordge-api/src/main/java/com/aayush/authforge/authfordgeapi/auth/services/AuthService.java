package com.aayush.authforge.authfordgeapi.auth.services;

import com.aayush.authforge.authfordgeapi.auth.io.RegisterRequest;
import com.aayush.authforge.authfordgeapi.user.io.UserResponse;

public interface AuthService {

    UserResponse registerLocalUser(RegisterRequest request);
}
