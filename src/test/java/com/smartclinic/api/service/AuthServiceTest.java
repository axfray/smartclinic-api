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
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
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
    private DoctorRepository doctorRepository;

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

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
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

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrow_whenEmailNull() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setPassword("admin123");

        assertThrows(IllegalArgumentException.class, () -> authService.login(dto));
    }

    @Test
    void login_shouldThrow_whenPasswordNull() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("admin@smartclinic.local");

        assertThrows(IllegalArgumentException.class, () -> authService.login(dto));
    }

    @Test
    void getCurrentUser_shouldResolveDoctorId_whenDoctor() {
        User user = User.builder()
                .id(3L)
                .firstName("Ana")
                .lastName("Lopez")
                .email("doc@test.local")
                .role(User.Role.ROLE_DOCTOR)
                .build();
        when(userRepository.findByEmail("doc@test.local")).thenReturn(Optional.of(user));
        when(doctorRepository.findByUserId(3L)).thenReturn(Optional.of(Doctor.builder().id(7L).build()));

        MeResponseDTO me = authService.getCurrentUser("doc@test.local");

        assertEquals(3L, me.getId());
        assertEquals("ROLE_DOCTOR", me.getRole());
        assertEquals(7L, me.getDoctorId());
    }

    @Test
    void getCurrentUser_shouldNotResolveDoctorId_whenPatient() {
        User user = User.builder()
                .id(4L)
                .email("patient@test.local")
                .role(User.Role.ROLE_PATIENT)
                .build();
        when(userRepository.findByEmail("patient@test.local")).thenReturn(Optional.of(user));

        MeResponseDTO me = authService.getCurrentUser("patient@test.local");

        assertEquals("ROLE_PATIENT", me.getRole());
        assertNull(me.getDoctorId());
    }

    @Test
    void getCurrentUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("nadie@test.local")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser("nadie@test.local"));
    }
}