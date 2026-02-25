package com.aayush.authforge.authfordgeapi.user.services;

import com.aayush.authforge.authfordgeapi.common.exceptions.InvalidPasswordException;
import com.aayush.authforge.authfordgeapi.common.exceptions.UserNotFoundException;
import com.aayush.authforge.authfordgeapi.entities.User;
import com.aayush.authforge.authfordgeapi.user.io.ChangePasswordRequest;
import com.aayush.authforge.authfordgeapi.user.io.UpdateProfileRequest;
import com.aayush.authforge.authfordgeapi.user.io.UserResponse;
import com.aayush.authforge.authfordgeapi.user.mapper.UserMapper;
import com.aayush.authforge.authfordgeapi.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    @Override
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public UserResponse getById(UUID id) {
        return userRepository.findById(id)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    @Override
    @Transactional
    public void enableUser(UUID userId) {
        User user = findUserById(userId);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disableUser(UUID userId) {
        User user = findUserById(userId);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);

        if (request.name() != null) {
            user.setName(request.name());
        }

        if (request.image() != null) {
            user.setProfilePicture(request.image());
        }

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findUserByEmail(email);
        if(passwordEncoder.matches(request.currentPassword(), user.getPassword())){
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            userRepository.save(user);
        }
        else{
            throw new InvalidPasswordException("Invalid Password");
        }
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        userRepository.deleteByEmail(email);
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}