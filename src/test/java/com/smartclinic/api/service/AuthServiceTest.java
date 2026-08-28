package com.smartclinic.api.service;

import com.smartclinic.api.dto.AuthResponseDTO;
import com.smartclinic.api.dto.LoginRequestDTO;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.UserRepository;
import com.smartclinic.api.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("admin@smartclinic.local");
        dto.setPassword("admin123");

        User user = User.builder()
                .id(1L)
                .firstName("Admin")
                .lastName("SmartClinic")
                .email("admin@smartclinic.local")
                .passwordHash("encodedHash")
                .role(User.Role.ROLE_ADMIN)
                .isActive(true)
                .build();

        when(userRepository.findByEmail("admin@smartclinic.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encodedHash")).thenReturn(true);
        when(jwtUtil.generateToken("admin@smartclinic.local")).thenReturn("token.jwt.valor");

        AuthResponseDTO response = authService.login(dto);

        assertNotNull(response);
        assertEquals("token.jwt.valor", response.getToken());
        assertEquals("admin@smartclinic.local", response.getEmail());
        assertEquals("ROLE_ADMIN", response.getRole());
        assertEquals("Admin", response.getFirstName());
        assertEquals("SmartClinic", response.getLastName());
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("nadie@mail.com");
        dto.setPassword("admin123");

        when(userRepository.findByEmail("nadie@mail.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrow_whenWrongPassword() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("admin@smartclinic.local");
        dto.setPassword("incorrecta");

        User user = User.builder()
                .id(1L)
                .email("admin@smartclinic.local")
                .passwordHash("encodedHash")
                .role(User.Role.ROLE_ADMIN)
                .build();

        when(userRepository.findByEmail("admin@smartclinic.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("incorrecta", "encodedHash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(dto));
    }
}