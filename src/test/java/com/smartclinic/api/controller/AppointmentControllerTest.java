package com.smartclinic.api.controller;

import com.smartclinic.api.model.Appointment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentControllerTest {

    @Test
    void parseStatus_shouldReturnEnum_whenValid() {
        assertEquals(Appointment.Status.PENDING, AppointmentController.parseStatus("pending"));
        assertEquals(Appointment.Status.CONFIRMED, AppointmentController.parseStatus("CONFIRMED"));
        assertEquals(Appointment.Status.CANCELLED, AppointmentController.parseStatus("cancelled"));
        assertEquals(Appointment.Status.COMPLETED, AppointmentController.parseStatus("Completed"));
    }

    @Test
    void parseStatus_shouldThrow_whenInvalid() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AppointmentController.parseStatus("COBRADO"));
        assertTrue(ex.getMessage().contains("Estado inválido"));
    }

    @Test
    void parseStatus_shouldThrow_whenNull() {
        assertThrows(IllegalArgumentException.class, () -> AppointmentController.parseStatus(null));
    }
}