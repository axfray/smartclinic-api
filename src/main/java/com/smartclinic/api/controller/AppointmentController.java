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

    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> scheduleAppointment(@RequestBody AppointmentRequestDTO dto) {
        AppointmentResponseDTO response = appointmentService.scheduleAppointment(
                dto.getPatientId(),
                dto.getDoctorId(),
                dto.getAppointmentDate(),
                dto.getReason()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<AppointmentResponseDTO> response = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(response);
    }
}