package com.smartclinic.api.service;

import com.smartclinic.api.dto.AuthResponseDTO;
import com.smartclinic.api.dto.LoginRequestDTO;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.UserRepository;
import com.smartclinic.api.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Credenciales inválidas.");
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponseDTO(
                token,
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getFirstName(),
                user.getLastName()
        );
    }
}
