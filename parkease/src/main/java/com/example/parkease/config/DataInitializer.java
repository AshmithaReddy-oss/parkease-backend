 package com.example.parkease.config;

import com.example.parkease.entity.User;
import com.example.parkease.enums.Role;
import com.example.parkease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (adminEmail.isBlank()
                    || adminPassword.isBlank()) {

                System.out.println(
                        "Admin initialization skipped: "
                        + "credentials were not configured."
                );

                return;
            }

            if (adminPassword.length() < 12) {
                throw new IllegalStateException(
                        "Initial admin password must contain "
                        + "at least 12 characters."
                );
            }

            if (userRepository.existsByEmail(adminEmail)) {

                System.out.println(
                        "Admin account already exists."
                );

                return;
            }

            User admin = new User();

            admin.setName("ParkEase Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(
                    passwordEncoder.encode(adminPassword)
            );
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);

            userRepository.save(admin);

            System.out.println(
                    "Initial ParkEase administrator created."
            );
        };
    }
}