CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ROLE_PATIENT','ROLE_DOCTOR','ROLE_ADMIN')),
    created_at    TIMESTAMP,
    is_active     BOOLEAN
);

CREATE TABLE specialties (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE doctors (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    specialty_id   BIGINT NOT NULL REFERENCES specialties(id),
    hourly_rate    NUMERIC(10,2)
);

CREATE TABLE doctor_schedules (
    id          BIGSERIAL PRIMARY KEY,
    doctor_id   BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time  TIME NOT NULL,
    end_time    TIME NOT NULL,
    CHECK (start_time < end_time)
);

CREATE TABLE appointments (
    id               BIGSERIAL PRIMARY KEY,
    patient_id       BIGINT NOT NULL REFERENCES users(id),
    doctor_id        BIGINT NOT NULL REFERENCES doctors(id),
    appointment_date TIMESTAMP NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED')),
    reason           VARCHAR(255),
    created_at       TIMESTAMP
);

CREATE TABLE medical_records (
    id             BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
    diagnosis      TEXT NOT NULL,
    treatment      TEXT,
    notes          TEXT,
    created_at     TIMESTAMP
);

CREATE UNIQUE INDEX idx_unique_active_doctor_appointment
    ON appointments (doctor_id, appointment_date)
    WHERE status <> 'CANCELLED';

CREATE INDEX idx_appointments_patient_date
    ON appointments (patient_id, appointment_date DESC);

CREATE INDEX idx_medical_records_appointment
    ON medical_records (appointment_id);