package com.smartclinic.api.controller;

import com.smartclinic.api.dto.AppointmentRequestDTO;
import com.smartclinic.api.dto.AppointmentResponseDTO;
import com.smartclinic.api.dto.AppointmentStatusRequestDTO;
import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(@Valid @RequestBody AppointmentRequestDTO dto) {
        AppointmentResponseDTO response = appointmentService.scheduleAppointment(
                dto.getPatientId(),
                dto.getDoctorId(),
                dto.getAppointmentDate(),
                dto.getReason()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentResponseDTO> response = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(@PathVariable Long id,
                                                               @Valid @RequestBody AppointmentStatusRequestDTO dto) {
        Appointment.Status status = Appointment.Status.valueOf(dto.getStatus());
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, status));
    }
}