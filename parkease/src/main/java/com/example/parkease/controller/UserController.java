package com.example.parkease.controller;

import com.example.parkease.entity.User;
import com.example.parkease.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get the currently logged-in user's profile
    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }
}