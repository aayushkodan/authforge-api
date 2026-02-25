package com.aayush.authforge.authfordgeapi.user.controllers;

import com.aayush.authforge.authfordgeapi.user.io.ChangePasswordRequest;
import com.aayush.authforge.authfordgeapi.user.io.UpdateProfileRequest;
import com.aayush.authforge.authfordgeapi.user.io.UserResponse;
import com.aayush.authforge.authfordgeapi.user.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class UserController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication){

        System.out.println(authentication.getName()+"wgres");
        return ResponseEntity.status(HttpStatus.OK).body(userService.getByEmail(authentication.getName()));
    }

    @PutMapping()
    public ResponseEntity<UserResponse> updateCurrentUser(Authentication authentication,@Valid @RequestBody UpdateProfileRequest user){
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateProfile(email,user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request){
        String email = authentication.getName();
        userService.changePassword(email, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication){
        String email = authentication.getName();
        userService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}
