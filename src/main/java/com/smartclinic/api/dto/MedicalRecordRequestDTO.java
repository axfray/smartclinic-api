package com.smartclinic.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicalRecordRequestDTO {

    @NotNull(message = "El appointmentId es obligatorio")
    private Long appointmentId;

    @NotBlank(message = "El diagnóstico es obligatorio")
    private String diagnosis;

    @Size(max = 4000)
    private String treatment;

    @Size(max = 4000)
    private String notes;
}
