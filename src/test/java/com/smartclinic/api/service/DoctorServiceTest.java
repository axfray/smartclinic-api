package com.smartclinic.api.service;

import com.smartclinic.api.dto.DoctorRequestDTO;
import com.smartclinic.api.dto.DoctorResponseDTO;
import com.smartclinic.api.dto.DoctorScheduleRequestDTO;
import com.smartclinic.api.dto.DoctorScheduleResponseDTO;
import com.smartclinic.api.model.Doctor;
import com.smartclinic.api.model.DoctorSchedule;
import com.smartclinic.api.model.Specialty;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.DoctorRepository;
import com.smartclinic.api.repository.DoctorScheduleRepository;
import com.smartclinic.api.repository.SpecialtyRepository;
import com.smartclinic.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void createDoctor_shouldThrow_whenUserIdNull() {
        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setLicenseNumber("M-123");

        assertThrows(IllegalArgumentException.class, () -> doctorService.createDoctor(dto));
    }

    @Test
    void createDoctor_shouldThrow_whenLicenseExists() {
        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setUserId(1L);
        dto.setLicenseNumber("M-123");
        when(doctorRepository.existsByLicenseNumber("M-123")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> doctorService.createDoctor(dto));
    }

    @Test
    void createDoctor_shouldReturnDTO() {
        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setUserId(1L);
        dto.setLicenseNumber("M-123");
        dto.setSpecialtyId(2L);
        dto.setHourlyRate(new BigDecimal("500.00"));

        User user = User.builder().id(1L).firstName("Maria").lastName("Gomez").build();
        Specialty specialty = Specialty.builder().id(2L).name("Cardiología").build();

        when(doctorRepository.existsByLicenseNumber("M-123")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(specialtyRepository.findById(2L)).thenReturn(Optional.of(specialty));

        Doctor doctor = Doctor.builder()
                .id(9L)
                .user(user)
                .licenseNumber("M-123")
                .specialty(specialty)
                .hourlyRate(new BigDecimal("500.00"))
                .build();
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorResponseDTO result = doctorService.createDoctor(dto);

        assertNotNull(result);
        assertEquals("Maria Gomez", result.getDoctorName());
        assertEquals("Cardiología", result.getSpecialtyName());
    }

    @Test
    void addSchedule_shouldThrow_whenDoctorNotFound() {
        DoctorScheduleRequestDTO dto = new DoctorScheduleRequestDTO();
        dto.setDoctorId(99L);
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> doctorService.addSchedule(dto));
    }

    @Test
    void addSchedule_shouldReturnDTO() {
        DoctorScheduleRequestDTO dto = new DoctorScheduleRequestDTO();
        dto.setDoctorId(1L);
        dto.setDayOfWeek(1);
        dto.setStartTime(java.time.LocalTime.of(9, 0));
        dto.setEndTime(java.time.LocalTime.of(12, 0));

        Doctor doctor = Doctor.builder().id(1L).build();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorSchedule schedule = DoctorSchedule.builder()
                .id(3L)
                .doctor(doctor)
                .dayOfWeek(1)
                .startTime(java.time.LocalTime.of(9, 0))
                .endTime(java.time.LocalTime.of(12, 0))
                .build();
        when(doctorScheduleRepository.save(any(DoctorSchedule.class))).thenReturn(schedule);

        DoctorScheduleResponseDTO result = doctorService.addSchedule(dto);

        assertNotNull(result);
        assertEquals(1, result.getDayOfWeek());
        assertEquals(1L, result.getDoctorId());
    }
}
