package com.smartclinic.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class DoctorScheduleRequestDTO {

    @NotNull(message = "El doctorId es obligatorio")
    private Long doctorId;

    @NotNull(message = "El día de la semana es obligatorio")
    @Min(value = 1, message = "dayOfWeek debe estar entre 1 (Lunes) y 7 (Domingo)")
    @Max(value = 7, message = "dayOfWeek debe estar entre 1 (Lunes) y 7 (Domingo)")
    private Integer dayOfWeek;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;
}
