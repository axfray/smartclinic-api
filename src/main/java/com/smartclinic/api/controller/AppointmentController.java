package com.smartclinic.api.controller;

import com.smartclinic.api.dto.AppointmentRequestDTO;
import com.smartclinic.api.dto.AppointmentResponseDTO;
import com.smartclinic.api.dto.AppointmentStatusRequestDTO;
import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.service.AppointmentService;
import com.smartclinic.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    public AppointmentController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        AppointmentResponseDTO response = appointmentService.scheduleAppointment(
                currentUserId(),
                dto.getDoctorId(),
                dto.getAppointmentDate(),
                dto.getReason()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByPatient(@PathVariable Long patientId) {
        if (!currentUserId().equals(patientId)) {
            throw new AccessDeniedException("No puede consultar los turnos de otro paciente.");
        }
        List<AppointmentResponseDTO> response = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(@PathVariable Long id,
                                                               @Valid @RequestBody AppointmentStatusRequestDTO dto) {
        Appointment.Status status = parseStatus(dto.getStatus());
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, status));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponseDTO> cancelOwnAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelOwnAppointment(id, currentUserId()));
    }

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email).getId();
    }

    static Appointment.Status parseStatus(String value) {
        if (value == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        try {
            return Appointment.Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Estado inválido. Valores válidos: PENDING, CONFIRMED, CANCELLED, COMPLETED.", ex);
        }
    }
}
