package com.smartclinic.api.controller;

import com.smartclinic.api.dto.AppointmentRequestDTO;
import com.smartclinic.api.dto.AppointmentResponseDTO;
import com.smartclinic.api.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Endpoint para reservar un nuevo turno
     * POST http://localhost:8080/api/appointments
     */
    @PostMapping
    public ResponseEntity<?> scheduleAppointment(@RequestBody AppointmentRequestDTO dto) {
        try {
            AppointmentResponseDTO response = appointmentService.scheduleAppointment(
                    dto.getPatientId(),
                    dto.getDoctorId(),
                    dto.getAppointmentDate(),
                    dto.getReason()
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint para consultar turnos por paciente
     * GET http://localhost:8080/api/appointments/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentResponseDTO> response = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(response);
    }
}