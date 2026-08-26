package com.smartclinic.api.repository;

import com.smartclinic.api.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Validar si el médico ya tiene un turno reservado en esa fecha/hora
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM Appointment a " +
           "WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDate = :date " +
           "AND a.status != 'CANCELLED'")
    boolean existsByDoctorIdAndAppointmentDate(
        @Param("doctorId") Long doctorId, 
        @Param("date") LocalDateTime date
    );

    // Listar turnos de un paciente
    List<Appointment> findByPatientId(Long patientId);
}