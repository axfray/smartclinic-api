package com.smartclinic.api.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class DoctorScheduleRequestDTO {
    private Long doctorId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
