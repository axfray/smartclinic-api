package com.smartclinic.api.service;

import com.smartclinic.api.dto.DoctorRequestDTO;
import com.smartclinic.api.dto.DoctorResponseDTO;
import com.smartclinic.api.dto.DoctorScheduleRequestDTO;
import com.smartclinic.api.dto.DoctorScheduleResponseDTO;
import com.smartclinic.api.model.Doctor;
import com.smartclinic.api.model.DoctorSchedule;
import com.smartclinic.api.model.Specialty;
import com.smartclinic.api.model.User;
import com.smartclinic.api.repository.DoctorRepository;
import com.smartclinic.api.repository.DoctorScheduleRepository;
import com.smartclinic.api.repository.SpecialtyRepository;
import com.smartclinic.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         DoctorScheduleRepository doctorScheduleRepository,
                         UserRepository userRepository,
                         SpecialtyRepository specialtyRepository) {
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
    }

    public DoctorResponseDTO createDoctor(DoctorRequestDTO dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("El userId es obligatorio.");
        }
        if (doctorRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new IllegalArgumentException("Ya existe un médico con esa matrícula.");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + dto.getUserId()));
        Specialty specialty = specialtyRepository.findById(dto.getSpecialtyId())
                .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada con id: " + dto.getSpecialtyId()));

        Doctor doctor = Doctor.builder()
                .user(user)
                .licenseNumber(dto.getLicenseNumber())
                .specialty(specialty)
                .hourlyRate(dto.getHourlyRate())
                .build();

        return mapToDTO(doctorRepository.save(doctor));
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado con id: " + id));
        return mapToDTO(doctor);
    }

    @Transactional
    public DoctorScheduleResponseDTO addSchedule(DoctorScheduleRequestDTO dto) {
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado con id: " + dto.getDoctorId()));

        if (dto.getDayOfWeek() == null || dto.getDayOfWeek() < 1 || dto.getDayOfWeek() > 7) {
            throw new IllegalArgumentException("dayOfWeek debe estar entre 1 (Lunes) y 7 (Domingo).");
        }
        if (dto.getStartTime() != null && dto.getEndTime() != null && !dto.getStartTime().isBefore(dto.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();

        return mapToScheduleDTO(doctorScheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponseDTO> getSchedulesByDoctor(Long doctorId) {
        return doctorScheduleRepository.findByDoctorId(doctorId).stream()
                .map(this::mapToScheduleDTO)
                .collect(Collectors.toList());
    }

    private DoctorResponseDTO mapToDTO(Doctor doctor) {
        DoctorResponseDTO dto = new DoctorResponseDTO();
        dto.setId(doctor.getId());
        if (doctor.getUser() != null) {
            dto.setUserId(doctor.getUser().getId());
            String name = doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName();
            dto.setDoctorName(name.trim());
        }
        dto.setLicenseNumber(doctor.getLicenseNumber());
        if (doctor.getSpecialty() != null) {
            dto.setSpecialtyId(doctor.getSpecialty().getId());
            dto.setSpecialtyName(doctor.getSpecialty().getName());
        }
        dto.setHourlyRate(doctor.getHourlyRate());
        return dto;
    }

    private DoctorScheduleResponseDTO mapToScheduleDTO(DoctorSchedule schedule) {
        DoctorScheduleResponseDTO dto = new DoctorScheduleResponseDTO();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctor() != null ? schedule.getDoctor().getId() : null);
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        return dto;
    }
}
