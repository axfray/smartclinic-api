package com.smartclinic.api.dto;

import com.smartclinic.api.model.User;
import lombok.Data;

@Data
public class UserRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private User.Role role;
    private Boolean isActive;
}
