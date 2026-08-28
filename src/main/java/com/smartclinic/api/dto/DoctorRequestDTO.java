package com.smartclinic.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorRequestDTO {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(max = 50)
    private String licenseNumber;

    @NotNull(message = "La especialidad es obligatoria")
    private Long specialtyId;

    @DecimalMin(value = "0.0", message = "La tarifa horaria no puede ser negativa")
    private BigDecimal hourlyRate;
}
