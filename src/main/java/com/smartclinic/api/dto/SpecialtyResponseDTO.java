package com.smartclinic.api.dto;

public class SpecialtyResponseDTO {

    private Long id;
    private String name;
    private String description;

    public SpecialtyResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
