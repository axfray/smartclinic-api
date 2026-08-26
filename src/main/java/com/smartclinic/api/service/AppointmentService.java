package com.smartclinic.api.service;

import com.smartclinic.api.dto.AppointmentResponseDTO;
import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Agendar un nuevo turno aplicando validaciones de negocio.
     */
    public AppointmentResponseDTO scheduleAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate, String reason) {

        // 1. Validar que la fecha elegida sea en el futuro
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede agendar un turno para una fecha u hora pasada.");
        }

        // 2. Validar que el médico no esté ocupado en ese horario
        boolean isDoctorOccupied = appointmentRepository.existsByDoctorIdAndAppointmentDate(doctorId, appointmentDate);
        if (isDoctorOccupied) {
            throw new IllegalStateException("El médico ya tiene una cita agendada en ese horario.");
        }

        // 3. Construir la entidad
        Appointment appointment = Appointment.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .appointmentDate(appointmentDate)
                .reason(reason)
                .status(Appointment.Status.PENDING)
                .build();

        // 4. Guardar en la base de datos
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 5. Retornar DTO de respuesta
        return mapToDTO(savedAppointment);
    }

    /**
     * Obtener el historial de turnos de un paciente mapeado a DTOs.
     */
    public List<AppointmentResponseDTO> getAppointmentsByPatient(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método mapeador privado: Convierte una entidad Appointment a DTO.
     */
    private AppointmentResponseDTO mapToDTO(Appointment appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatientId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setReason(appointment.getReason());
        dto.setStatus(appointment.getStatus().name());
        return dto;
    }
}