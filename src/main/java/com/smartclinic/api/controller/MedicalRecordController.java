package com.smartclinic.api.controller;

import com.smartclinic.api.dto.MedicalRecordRequestDTO;
import com.smartclinic.api.dto.MedicalRecordResponseDTO;
import com.smartclinic.api.service.MedicalRecordService;
import com.smartclinic.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final UserService userService;

    public MedicalRecordController(MedicalRecordService medicalRecordService, UserService userService) {
        this.medicalRecordService = medicalRecordService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponseDTO> createRecord(@Valid @RequestBody MedicalRecordRequestDTO dto) {
        MedicalRecordResponseDTO response = medicalRecordService.createRecord(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<MedicalRecordResponseDTO>> getAllRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllRecords());
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MedicalRecordResponseDTO> getRecordByAppointment(@PathVariable Long appointmentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isStaff = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_DOCTOR") || a.equals("ROLE_ADMIN"));
        Long currentUserId = isStaff ? null : userService.getUserByEmail(auth.getName()).getId();
        return ResponseEntity.ok(medicalRecordService.getRecordByAppointment(appointmentId, currentUserId, isStaff));
    }
}
