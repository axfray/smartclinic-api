package com.smartclinic.api.service;

import com.smartclinic.api.dto.SpecialtyRequestDTO;
import com.smartclinic.api.dto.SpecialtyResponseDTO;
import com.smartclinic.api.model.Specialty;
import com.smartclinic.api.repository.SpecialtyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private SpecialtyService specialtyService;

    @Test
    void createSpecialty_shouldThrow_whenNameExists() {
        SpecialtyRequestDTO dto = new SpecialtyRequestDTO();
        dto.setName("Cardiología");
        when(specialtyRepository.existsByName("Cardiología")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> specialtyService.createSpecialty(dto));
    }

    @Test
    void createSpecialty_shouldReturnDTO() {
        SpecialtyRequestDTO dto = new SpecialtyRequestDTO();
        dto.setName("Cardiología");
        dto.setDescription("Del corazón");

        Specialty saved = Specialty.builder().id(1L).name("Cardiología").description("Del corazón").build();
        when(specialtyRepository.existsByName("Cardiología")).thenReturn(false);
        when(specialtyRepository.save(any(Specialty.class))).thenReturn(saved);

        SpecialtyResponseDTO result = specialtyService.createSpecialty(dto);

        assertNotNull(result);
        assertEquals("Cardiología", result.getName());
    }

    @Test
    void getSpecialtyById_shouldThrow_whenNotFound() {
        when(specialtyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> specialtyService.getSpecialtyById(99L));
    }
}
