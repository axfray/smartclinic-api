package com.smartclinic.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppointmentStatusRequestDTO {

    @NotBlank(message = "El estado es obligatorio")
    private String status;
}
