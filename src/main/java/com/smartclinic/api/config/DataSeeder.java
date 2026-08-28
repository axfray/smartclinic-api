package com.smartclinic.api.config;

import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@smartclinic.local}")
    private String adminEmail;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        User admin = User.builder()
                .firstName("Admin")
                .lastName("SmartClinic")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(User.Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);
    }
}