package com.smartclinic.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeResponseDTO {

    private Long id;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private Long doctorId;
}
