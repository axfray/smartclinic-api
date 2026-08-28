package com.smartclinic.api.service;

import com.smartclinic.api.dto.MedicalRecordRequestDTO;
import com.smartclinic.api.dto.MedicalRecordResponseDTO;
import com.smartclinic.api.model.Appointment;
import com.smartclinic.api.model.MedicalRecord;
import com.smartclinic.api.repository.AppointmentRepository;
import com.smartclinic.api.repository.MedicalRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @Test
    void createRecord_shouldThrow_whenAppointmentNotFound() {
        MedicalRecordRequestDTO dto = new MedicalRecordRequestDTO();
        dto.setAppointmentId(99L);
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> medicalRecordService.createRecord(dto));
    }

    @Test
    void createRecord_shouldThrow_whenRecordAlreadyExists() {
        MedicalRecordRequestDTO dto = new MedicalRecordRequestDTO();
        dto.setAppointmentId(1L);
        Appointment appointment = Appointment.builder().id(1L).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.findByAppointmentId(1L)).thenReturn(Optional.of(new MedicalRecord()));

        assertThrows(IllegalArgumentException.class, () -> medicalRecordService.createRecord(dto));
    }

    @Test
    void createRecord_shouldReturnDTO() {
        MedicalRecordRequestDTO dto = new MedicalRecordRequestDTO();
        dto.setAppointmentId(1L);
        dto.setDiagnosis("Gripe");
        dto.setTreatment("Paracetamol");

        Appointment appointment = Appointment.builder().id(1L).build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(medicalRecordRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());

        MedicalRecord record = MedicalRecord.builder()
                .id(2L)
                .appointment(appointment)
                .diagnosis("Gripe")
                .treatment("Paracetamol")
                .build();
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);

        MedicalRecordResponseDTO result = medicalRecordService.createRecord(dto);

        assertNotNull(result);
        assertEquals("Gripe", result.getDiagnosis());
        assertEquals(1L, result.getAppointmentId());
    }
}
