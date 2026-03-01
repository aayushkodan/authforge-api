package com.aayush.authforge.authfordgeapi.auth.services;

import com.aayush.authforge.authfordgeapi.common.exceptions.EmailAlreadyExistsException;
import com.aayush.authforge.authfordgeapi.common.exceptions.RoleNotFoundException;
import com.aayush.authforge.authfordgeapi.entities.Provider;
import com.aayush.authforge.authfordgeapi.entities.Role;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.role.repositories.RoleRepository;
import com.aayush.authforge.authfordgeapi.auth.io.RegisterRequest;
import com.aayush.authforge.authfordgeapi.user.io.UserResponse;
import com.aayush.authforge.authfordgeapi.user.mapper.UserMapper;
import com.aayush.authforge.authfordgeapi.user.repositories.UserRepository;
import com.aayush.authforge.authfordgeapi.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerLocalUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Default role not found"));

        User user = User.builder()
                .name(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .provider(Provider.LOCAL)
                .enabled(false)
                .roles(Set.of(defaultRole))
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }
}
