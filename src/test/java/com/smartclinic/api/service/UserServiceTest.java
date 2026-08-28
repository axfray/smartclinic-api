package com.smartclinic.api.service;

import com.smartclinic.api.dto.UserRequestDTO;
import com.smartclinic.api.dto.UserResponseDTO;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldThrow_whenEmailAlreadyExists() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("juan@mail.com");
        when(userRepository.existsByEmail("juan@mail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
    }

    @Test
    void createUser_shouldReturnDTO() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFirstName("Juan");
        dto.setLastName("Perez");
        dto.setEmail("juan@mail.com");
        dto.setPasswordHash("hash");
        dto.setRole(User.Role.ROLE_PATIENT);

        User saved = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@mail.com")
                .role(User.Role.ROLE_PATIENT)
                .isActive(true)
                .build();
        when(userRepository.existsByEmail("juan@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("hash")).thenReturn("encodedHash");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponseDTO result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("Juan Perez", result.getFirstName() + " " + result.getLastName());
        assertEquals("ROLE_PATIENT", result.getRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encodedHash", captor.getValue().getPasswordHash());
    }

    @Test
    void updateUser_shouldThrow_whenEmailAlreadyTaken() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail("otro@mail.com");

        User existing = User.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@mail.com")
                .role(User.Role.ROLE_PATIENT)
                .isActive(true)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("otro@mail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, dto));
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getUserById(99L));
    }
}
