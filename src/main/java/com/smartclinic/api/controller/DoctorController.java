package com.smartclinic.api.controller;

import com.smartclinic.api.dto.DoctorRequestDTO;
import com.smartclinic.api.dto.DoctorResponseDTO;
import com.smartclinic.api.dto.DoctorScheduleRequestDTO;
import com.smartclinic.api.dto.DoctorScheduleResponseDTO;
import com.smartclinic.api.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid @RequestBody DoctorRequestDTO dto) {
        DoctorResponseDTO response = doctorService.createDoctor(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorScheduleResponseDTO> addSchedule(@Valid @RequestBody DoctorScheduleRequestDTO dto) {
        DoctorScheduleResponseDTO response = doctorService.addSchedule(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<DoctorScheduleResponseDTO>> getSchedulesByDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getSchedulesByDoctor(id));
    }
}
