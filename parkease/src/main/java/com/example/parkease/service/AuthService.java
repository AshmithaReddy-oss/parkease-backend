package com.example.parkease.service;

import com.example.parkease.dto.AuthResponse;
import com.example.parkease.dto.LoginRequest;
import com.example.parkease.dto.RegisterRequest;

import com.example.parkease.entity.User;
import com.example.parkease.enums.Role;

import com.example.parkease.exception.ResourceNotFoundException;
import com.example.parkease.repository.UserRepository;
import com.example.parkease.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // =========================
    // REGISTER
    // =========================
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Name is required."
            );
        }

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (request.getPassword() == null
                || request.getPassword().length() < 8) {

            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        if (request.getRole() == null) {

            throw new IllegalArgumentException(
                    "Role is required."
            );
        }

        // Never allow public administrator registration
        if (request.getRole() == Role.ADMIN) {

            throw new IllegalArgumentException(
                    "Administrator accounts cannot be created "
                    + "through public registration."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {

            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);

        // Store only the encrypted password
        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    // =========================
    // LOGIN
    // =========================
    public AuthResponse login(LoginRequest request) {

        if (request.getEmail() == null
                || request.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required."
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        Authentication authentication =
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                    )
                );

        String authenticatedEmail =
                authentication.getName();

        User user = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "User account not found."
                    )
                );

        if (!user.isEnabled()) {

            throw new IllegalStateException(
                    "This account is disabled."
            );
        }

        String token =
                jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}