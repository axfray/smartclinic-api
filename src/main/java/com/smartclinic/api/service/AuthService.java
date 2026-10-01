package com.smartclinic.api.service;

import com.smartclinic.api.dto.AuthResponseDTO;
import com.smartclinic.api.dto.LoginRequestDTO;
import com.smartclinic.api.dto.MeResponseDTO;
import com.smartclinic.api.exception.ResourceNotFoundException;
import com.smartclinic.api.model.Doctor;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.DoctorRepository;
import com.smartclinic.api.repository.UserRepository;
import com.smartclinic.api.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, DoctorRepository doctorRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public MeResponseDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        Long doctorId = null;
        if (user.getRole() == User.Role.ROLE_DOCTOR) {
            doctorId = doctorRepository.findByUserId(user.getId())
                    .map(Doctor::getId)
                    .orElse(null);
        }

        return MeResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .doctorId(doctorId)
                .build();
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
             throw new IllegalArgumentException("Credenciales inválidas.");
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas."));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
             throw new BadCredentialsException("Credenciales inválidas.");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
             throw new DisabledException("El usuario está desactivado.");
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
