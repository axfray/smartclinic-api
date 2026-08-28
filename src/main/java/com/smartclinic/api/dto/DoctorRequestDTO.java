package com.smartclinic.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorRequestDTO {
    private Long userId;
    private String licenseNumber;
    private Long specialtyId;
    private BigDecimal hourlyRate;
}
