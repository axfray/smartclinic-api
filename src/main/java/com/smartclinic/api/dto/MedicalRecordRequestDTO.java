package com.smartclinic.api.dto;

import lombok.Data;

@Data
public class MedicalRecordRequestDTO {
    private Long appointmentId;
    private String diagnosis;
    private String treatment;
    private String notes;
}
