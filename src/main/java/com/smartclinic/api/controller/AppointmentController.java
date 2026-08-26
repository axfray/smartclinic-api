package com.smartclinic.api.controller;

import com.smartclinic.api.dto.AppointmentRequestDTO;
import com.smartclinic.api.model.Appointment;
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
            Appointment appointment = appointmentService.scheduleAppointment(
                    dto.getPatientId(),
                    dto.getDoctorId(),
                    dto.getAppointmentDate(),
                    dto.getReason()
            );
            return new ResponseEntity<>(appointment, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Devuelve error 400 Bad Request con el mensaje de validación de negocio
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Endpoint para consultar turnos de un paciente
     * GET http://localhost:8080/api/appointments/patient/1
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByPatient(@PathVariable Long patientId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointments);
    }
}