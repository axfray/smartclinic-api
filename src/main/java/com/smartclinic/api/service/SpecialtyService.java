package com.smartclinic.api.service;

import com.smartclinic.api.dto.SpecialtyRequestDTO;
import com.smartclinic.api.dto.SpecialtyResponseDTO;
import com.smartclinic.api.model.Specialty;
import com.smartclinic.api.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    public SpecialtyResponseDTO createSpecialty(SpecialtyRequestDTO dto) {
        if (specialtyRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe una especialidad con ese nombre.");
        }

        Specialty specialty = Specialty.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        return mapToDTO(specialtyRepository.save(specialty));
    }

    public List<SpecialtyResponseDTO> getAllSpecialties() {
        return specialtyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public SpecialtyResponseDTO getSpecialtyById(Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada con id: " + id));
        return mapToDTO(specialty);
    }

    public void deleteSpecialty(Long id) {
        if (!specialtyRepository.existsById(id)) {
            throw new IllegalArgumentException("Especialidad no encontrada con id: " + id);
        }
        specialtyRepository.deleteById(id);
    }

    private SpecialtyResponseDTO mapToDTO(Specialty specialty) {
        SpecialtyResponseDTO dto = new SpecialtyResponseDTO();
        dto.setId(specialty.getId());
        dto.setName(specialty.getName());
        dto.setDescription(specialty.getDescription());
        return dto;
    }
}
