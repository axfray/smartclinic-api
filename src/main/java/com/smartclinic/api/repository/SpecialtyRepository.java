package com.smartclinic.api.repository;

import com.smartclinic.api.model.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    boolean existsByName(String name);
}
