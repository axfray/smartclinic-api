-- 1. TABLA DE USUARIOS (Autenticación y Roles)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ROLE_PATIENT', 'ROLE_DOCTOR', 'ROLE_ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 2. TABLA DE ESPECIALIDADES MÉDICAS
CREATE TABLE specialties (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- 3. TABLA DE MÉDICOS (Extensión del perfil de usuario)
CREATE TABLE doctors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    license_number VARCHAR(50) NOT NULL UNIQUE, -- Matrícula profesional
    specialty_id INT NOT NULL,
    hourly_rate NUMERIC(10, 2) DEFAULT 0.00,
    CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_doctors_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id)
);

-- 4. TABLA DE DISPONIBILIDAD HORARIA DE MÉDICOS
CREATE TABLE doctor_schedules (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1: Lunes, 7: Domingo
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT fk_schedules_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    CONSTRAINT chk_schedule_times CHECK (start_time < end_time)
);

-- 5. TABLA DE TURNOS / CITAS MÉDICAS
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES users(id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- 6. TABLA DE HISTORIAL / NOTAS CLÍNICAS (Relación 1 a 1 con Turnos)
CREATE TABLE medical_records (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    diagnosis TEXT NOT NULL,
    treatment TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_records_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);




-- 1. ÍNDICE ÚNICO PARCIAL:
-- Evita que un médico tenga dos turnos en la misma fecha y hora (omite turnos cancelados).
CREATE UNIQUE INDEX idx_unique_active_doctor_appointment 
ON appointments (doctor_id, appointment_date) 
WHERE status != 'CANCELLED';

-- 2. ÍNDICE DE BÚSQUEDA RÁPIDA:
-- Optimiza la consulta de turnos de un paciente por fecha.
CREATE INDEX idx_appointments_patient_date 
ON appointments (patient_id, appointment_date DESC);

-- 3. ÍNDICE PARA HISTORIAL CLÍNICO:
-- Agiliza la búsqueda de registros médicos por diagnóstico o notas.
CREATE INDEX idx_medical_records_appointment 
ON medical_records (appointment_id);




-- Especialidades de ejemplo
INSERT INTO specialties (name, description) VALUES 
('Cardiología', 'Enfermedades del corazón y sistema circulatorio'),
('Pediatría', 'Atención médica de bebés, niños y adolescentes'),
('Traumatología', 'Lesiones del sistema musculoesquelético');

-- Usuario Administrador por defecto
INSERT INTO users (first_name, last_name, email, password_hash, role) VALUES 
('Admin', 'System', 'ferreyraaxel40gmail.com', '1234.', 'ROLE_ADMIN');