package com.smartclinic.api.service;

import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    // Inyección de dependencias por constructor
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Agendar un nuevo turno aplicando validaciones de negocio.
     */
    public Appointment scheduleAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate, String reason) {

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

        // 4. Guardar en PostgreSQL a través del repositorio
        return appointmentRepository.save(appointment);
    }

    /**
     * Obtener el historial de turnos de un paciente.
     */
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }
}
