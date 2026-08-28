package com.smartclinic.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequestDTO {

    @NotNull(message = "El patientId es obligatorio")
    private Long patientId;

    @NotNull(message = "El doctorId es obligatorio")
    private Long doctorId;

    @NotNull(message = "La fecha del turno es obligatoria")
    @Future(message = "La fecha del turno debe ser futura")
    private LocalDateTime appointmentDate;

    @Size(max = 255)
    private String reason;
}
