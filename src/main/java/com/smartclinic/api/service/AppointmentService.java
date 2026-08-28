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
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              UserRepository userRepository,
                              DoctorRepository doctorRepository,
                              DoctorScheduleRepository doctorScheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    /**
     * Agendar un nuevo turno aplicando validaciones de negocio.
     */
    public AppointmentResponseDTO scheduleAppointment(Long patientId, Long doctorId, LocalDateTime appointmentDate, String reason) {

        // 0. Validar que los datos obligatorios no sean nulos
        if (patientId == null) {
            throw new IllegalArgumentException("El patientId es obligatorio.");
        }
        if (doctorId == null) {
            throw new IllegalArgumentException("El doctorId es obligatorio.");
        }
        if (appointmentDate == null) {
            throw new IllegalArgumentException("La fecha del turno es obligatoria.");
        }

        // 1. Validar que la fecha elegida sea en el futuro
        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede agendar un turno para una fecha u hora pasada.");
        }

        // 2. Validar que el paciente exista
        if (!userRepository.existsById(patientId)) {
            throw new IllegalArgumentException("El paciente no existe.");
        }

        // 3. Validar que el médico exista y tenga disponibilidad ese día y horario
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("El médico no existe."));
        validateDoctorAvailability(doctor, appointmentDate);

        // 4. Validar que el médico no esté ocupado en ese horario
        boolean isDoctorOccupied = appointmentRepository.existsByDoctorIdAndAppointmentDate(doctorId, appointmentDate);
        if (isDoctorOccupied) {
            throw new IllegalStateException("El médico ya tiene una cita agendada en ese horario.");
        }

        // 5. Construir la entidad
        Appointment appointment = Appointment.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .appointmentDate(appointmentDate)
                .reason(reason)
                .status(Appointment.Status.PENDING)
                .build();

        // 6. Guardar en la base de datos
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 7. Retornar DTO de respuesta
        return mapToDTO(savedAppointment);
    }

    /**
     * Actualizar el estado de un turno (confirmar, cancelar, completar).
     */
    public AppointmentResponseDTO updateAppointmentStatus(Long appointmentId, Appointment.Status status) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("El id del turno es obligatorio.");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado con id: " + appointmentId));

        if (appointment.getStatus() == Appointment.Status.COMPLETED
                || appointment.getStatus() == Appointment.Status.CANCELLED) {
            throw new IllegalStateException("No se puede cambiar el estado de un turno " + appointment.getStatus().name() + ".");
        }
        if (appointment.getStatus() == Appointment.Status.CONFIRMED && status == Appointment.Status.PENDING) {
            throw new IllegalStateException("Un turno confirmado no puede volver a estado pendiente.");
        }

        appointment.setStatus(status);
        return mapToDTO(appointmentRepository.save(appointment));
    }

    private void validateDoctorAvailability(Doctor doctor, LocalDateTime appointmentDate) {
        DayOfWeek dayOfWeek = appointmentDate.getDayOfWeek();
        int dayValue = dayOfWeek.getValue(); // 1 = Lunes ... 7 = Domingo

        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doctor.getId()).stream()
                .filter(s -> s.getDayOfWeek() != null && s.getDayOfWeek() == dayValue)
                .collect(Collectors.toList());

        if (schedules.isEmpty()) {
            throw new IllegalStateException("El médico no atiende ese día de la semana.");
        }

        LocalTime time = appointmentDate.toLocalTime();
        boolean withinSchedule = schedules.stream()
                .anyMatch(s -> (s.getStartTime() == null || !time.isBefore(s.getStartTime()))
                        && (s.getEndTime() == null || !time.isAfter(s.getEndTime())));
        if (!withinSchedule) {
            throw new IllegalStateException("El horario elegido está fuera de la disponibilidad del médico.");
        }
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
        dto.setPatientName(resolvePatientName(appointment.getPatientId()));
        dto.setDoctorId(appointment.getDoctorId());
        dto.setDoctorName(resolveDoctorName(appointment.getDoctorId()));
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setReason(appointment.getReason());
        dto.setStatus(appointment.getStatus() != null ? appointment.getStatus().name() : null);
        return dto;
    }

    private String resolvePatientName(Long patientId) {
        return userRepository.findById(patientId)
                .map(this::fullName)
                .orElse(null);
    }

    private String resolveDoctorName(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .map(Doctor::getUser)
                .map(this::fullName)
                .orElse(null);
    }

    private String fullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}