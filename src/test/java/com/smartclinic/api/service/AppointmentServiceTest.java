package com.smartclinic.api.service;

import com.smartclinic.api.dto.AppointmentResponseDTO;
import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.model.Doctor;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.AppointmentRepository;
import com.smartclinic.api.repository.DoctorRepository;
import com.smartclinic.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void scheduleAppointment_shouldThrow_whenDateIsInPast() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(1L, 1L, past, "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldThrow_whenDoctorOccupied() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(1L, future)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> appointmentService.scheduleAppointment(1L, 1L, future, "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldReturnDTO_withNamesResolved() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(1L, future)).thenReturn(false);

        Appointment saved = Appointment.builder()
                .id(10L)
                .patientId(5L)
                .doctorId(1L)
                .appointmentDate(future)
                .status(Appointment.Status.PENDING)
                .reason("Consulta general")
                .build();
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

        User patient = User.builder().firstName("Juan").lastName("Perez").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(patient));

        User doctorUser = User.builder().firstName("Maria").lastName("Gomez").build();
        Doctor doctor = Doctor.builder().user(doctorUser).build();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        AppointmentResponseDTO dto = appointmentService.scheduleAppointment(5L, 1L, future, "Consulta general");

        assertNotNull(dto);
        assertEquals("Juan Perez", dto.getPatientName());
        assertEquals("Maria Gomez", dto.getDoctorName());
        assertEquals("PENDING", dto.getStatus());
    }

    @Test
    void getAppointmentsByPatient_shouldReturnList() {
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patientId(5L)
                .doctorId(1L)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .status(Appointment.Status.PENDING)
                .build();
        when(appointmentRepository.findByPatientId(5L)).thenReturn(List.of(appointment));

        List<AppointmentResponseDTO> result = appointmentService.getAppointmentsByPatient(5L);

        assertEquals(1, result.size());
    }
}
