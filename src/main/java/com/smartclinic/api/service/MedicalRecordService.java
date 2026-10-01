package com.smartclinic.api.service;

import com.smartclinic.api.dto.MedicalRecordRequestDTO;
import com.smartclinic.api.dto.MedicalRecordResponseDTO;
import com.smartclinic.api.exception.ConflictException;
import com.smartclinic.api.exception.ResourceNotFoundException;
import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.model.MedicalRecord;
import com.smartclinic.api.repository.AppointmentRepository;
import com.smartclinic.api.repository.MedicalRecordRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                AppointmentRepository appointmentRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public MedicalRecordResponseDTO createRecord(MedicalRecordRequestDTO dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con id: " + dto.getAppointmentId()));

        if (medicalRecordRepository.findByAppointmentId(dto.getAppointmentId()).isPresent()) {
            throw new ConflictException("El turno ya tiene un registro clínico asociado.");
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(medicalRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponseDTO getRecordByAppointment(Long appointmentId, Long requestingUserId, boolean isStaff) {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un registro clínico para el turno: " + appointmentId));

        if (!isStaff) {
            Long ownerId = record.getAppointment() != null ? record.getAppointment().getPatientId() : null;
            if (ownerId == null || !ownerId.equals(requestingUserId)) {
                throw new AccessDeniedException("No puede consultar el historial de otro paciente.");
            }
        }

        return mapToDTO(record);
    }

    public List<MedicalRecordResponseDTO> getAllRecords() {
        return medicalRecordRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private MedicalRecordResponseDTO mapToDTO(MedicalRecord record) {
        MedicalRecordResponseDTO dto = new MedicalRecordResponseDTO();
        dto.setId(record.getId());
        dto.setAppointmentId(record.getAppointment() != null ? record.getAppointment().getId() : null);
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatment(record.getTreatment());
        dto.setNotes(record.getNotes());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
