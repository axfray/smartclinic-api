package com.smartclinic.api.service;

import com.smartclinic.api.dto.AppointmentResponseDTO;
import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.model.Doctor;
import com.smartclinic.api.model.DoctorSchedule;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.AppointmentRepository;
import com.smartclinic.api.repository.DoctorRepository;
import com.smartclinic.api.repository.DoctorScheduleRepository;
import com.smartclinic.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void scheduleAppointment_shouldThrow_whenDateIsInPast() {
        LocalDateTime past = LocalDateTime.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(1L, 1L, past, "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldThrow_whenPatientIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(null, 1L, LocalDateTime.now().plusDays(1), "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldThrow_whenDoctorIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(1L, null, LocalDateTime.now().plusDays(1), "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldThrow_whenAppointmentDateNull() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(1L, 1L, null, "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldThrow_whenPatientDoesNotExist() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.scheduleAppointment(1L, 1L, future, "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldThrow_whenDoctorHasNoScheduleThatDay() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctorWithId(1L)));
        when(doctorScheduleRepository.findByDoctorId(1L)).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> appointmentService.scheduleAppointment(1L, 1L, future, "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldThrow_whenDoctorOccupied() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctorWithId(1L)));
        when(doctorScheduleRepository.findByDoctorId(1L)).thenReturn(List.of(fullDayScheduleFor(future)));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(1L, future)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> appointmentService.scheduleAppointment(1L, 1L, future, "Consulta"));
    }

    @Test
    void scheduleAppointment_shouldReturnDTO_withNamesResolved() {
        LocalDateTime future = LocalDateTime.now().plusDays(1);
        when(userRepository.existsById(5L)).thenReturn(true);

        User doctorUser = User.builder().firstName("Maria").lastName("Gomez").build();
        Doctor doctor = Doctor.builder().id(1L).user(doctorUser).build();
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorId(1L)).thenReturn(List.of(fullDayScheduleFor(future)));
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

    @Test
    void updateAppointmentStatus_shouldUpdate() {
        Appointment appointment = Appointment.builder()
                .id(10L)
                .patientId(5L)
                .doctorId(1L)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .status(Appointment.Status.PENDING)
                .build();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDTO dto = appointmentService.updateAppointmentStatus(10L, Appointment.Status.CONFIRMED);

        assertEquals("CONFIRMED", dto.getStatus());
        assertEquals("CONFIRMED", appointment.getStatus().name());
    }

    @Test
    void updateAppointmentStatus_shouldThrow_whenNotFound() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateAppointmentStatus(99L, Appointment.Status.CONFIRMED));
    }

    @Test
    void updateAppointmentStatus_shouldThrow_whenAppointmentIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateAppointmentStatus(null, Appointment.Status.CONFIRMED));
    }

    @Test
    void updateAppointmentStatus_shouldThrow_whenStatusNull() {
        assertThrows(IllegalArgumentException.class,
                () -> appointmentService.updateAppointmentStatus(10L, null));
    }

    @Test
    void getAppointmentsByPatient_shouldHandleNullStatus() {
        Appointment appointment = Appointment.builder()
                .id(1L)
                .patientId(5L)
                .doctorId(1L)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .status(null)
                .build();
        when(appointmentRepository.findByPatientId(5L)).thenReturn(List.of(appointment));

        List<AppointmentResponseDTO> result = appointmentService.getAppointmentsByPatient(5L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getStatus());
    }

    @Test
    void updateAppointmentStatus_shouldThrow_whenAlreadyCompleted() {
        Appointment appointment = Appointment.builder()
                .id(10L)
                .patientId(5L)
                .doctorId(1L)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .status(Appointment.Status.COMPLETED)
                .build();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateAppointmentStatus(10L, Appointment.Status.CANCELLED));
    }

    @Test
    void updateAppointmentStatus_shouldThrow_whenConfirmedToPending() {
        Appointment appointment = Appointment.builder()
                .id(10L)
                .patientId(5L)
                .doctorId(1L)
                .appointmentDate(LocalDateTime.now().plusDays(1))
                .status(Appointment.Status.CONFIRMED)
                .build();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateAppointmentStatus(10L, Appointment.Status.PENDING));
    }

    private Doctor doctorWithId(Long id) {
        return Doctor.builder().id(id).build();
    }

    private DoctorSchedule fullDayScheduleFor(LocalDateTime date) {
        return DoctorSchedule.builder()
                .dayOfWeek(date.getDayOfWeek().getValue())
                .startTime(LocalTime.MIN)
                .endTime(LocalTime.MAX)
                .build();
    }
}