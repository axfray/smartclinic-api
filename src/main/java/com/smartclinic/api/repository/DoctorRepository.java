package com.smartclinic.api.repository;

import com.smartclinic.api.model.Doctor;
import com.smartclinic.api.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findBySpecialty(Specialty specialty);

    boolean existsByLicenseNumber(String licenseNumber);
}
