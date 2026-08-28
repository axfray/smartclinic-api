# AGENTS.md - SmartClinic API

## Visión General

API RESTful para la gestión integral de turnos médicos y sincronización de pacientes y profesionales de salud. Proyecto en desarrollo activo con una base de datos PostgreSQL que define un esquema completo (6 tablas) implementado en código Java.

## Stack Tecnológico

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.2.3
- **Persistencia:** Spring Data JPA + PostgreSQL
- **Seguridad:** Spring Security (configuración abierta, sin autenticación real aún)
- **Documentación:** SpringDoc OpenAPI (Swagger UI en `/swagger-ui/index.html`)
- **Build Tool:** Maven (wrapper incluido)
- **Librería:** Lombok

## Arquitectura

Arquitectura en capas (Layered Architecture) bajo el package `com.smartclinic.api`:

```
com.smartclinic.api/
├── ApiApplication.java              # Entry point
├── config/
│   └── SecurityConfig.java          # Configuración de Spring Security
├── controller/
│   ├── AppointmentController.java   # Turnos
│   ├── UserController.java          # Usuarios
│   ├── DoctorController.java        # Médicos + horarios
│   ├── SpecialtyController.java     # Especialidades
│   └── MedicalRecordController.java # Historial clínico
├── service/
│   ├── AppointmentService.java      # Lógica de turnos
│   ├── UserService.java             # Lógica de usuarios
│   ├── DoctorService.java           # Lógica de médicos y horarios
│   ├── SpecialtyService.java        # Lógica de especialidades
│   └── MedicalRecordService.java    # Lógica de historial clínico
├── repository/
│   ├── AppointmentRepository.java
│   ├── UserRepository.java
│   ├── DoctorRepository.java
│   ├── DoctorScheduleRepository.java
│   ├── SpecialtyRepository.java
│   └── MedicalRecordRepository.java
├── model/
│   ├── Appointment.java
│   ├── User.java
│   ├── Doctor.java
│   ├── DoctorSchedule.java
│   ├── Specialty.java
│   └── MedicalRecord.java
├── dto/
│   ├── *RequestDTO.java             # DTOs de entrada
│   ├── *ResponseDTO.java            # DTOs de salida
│   └── ErrorResponseDTO.java        # DTO de error
└── exception/
    └── GlobalExceptionHandler.java  # Manejo global de excepciones
```

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/appointments` | Agenda un nuevo turno (201) |
| GET | `/api/appointments/patient/{patientId}` | Turnos de un paciente (200) |
| POST | `/api/users` | Crea un usuario (201) |
| GET | `/api/users` | Lista usuarios (200) |
| GET | `/api/users/{id}` | Usuario por id (200) |
| PUT | `/api/users/{id}` | Actualiza usuario (200) |
| DELETE | `/api/users/{id}` | Elimina usuario (204) |
| POST | `/api/specialties` | Crea una especialidad (201) |
| GET | `/api/specialties` | Lista especialidades (200) |
| GET | `/api/specialties/{id}` | Especialidad por id (200) |
| DELETE | `/api/specialties/{id}` | Elimina especialidad (204) |
| POST | `/api/doctors` | Crea un médico (201) |
| GET | `/api/doctors` | Lista médicos (200) |
| GET | `/api/doctors/{id}` | Médico por id (200) |
| POST | `/api/doctors/schedules` | Agrega horario a un médico (201) |
| GET | `/api/doctors/{id}/schedules` | Horarios de un médico (200) |
| POST | `/api/medical-records` | Crea un registro clínico (201) |
| GET | `/api/medical-records` | Lista registros clínicos (200) |
| GET | `/api/medical-records/appointment/{appointmentId}` | Registro por turno (200) |

## Base de Datos

El `schema.sql` define 6 tablas, todas implementadas en Java:

- `users` — Usuarios con roles (ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN)
- `specialties` — Especialidades médicas
- `doctors` — Perfiles de médicos (FK a users y specialties)
- `doctor_schedules` — Disponibilidad horaria de médicos
- `appointments` — Turnos médicos
- `medical_records` — Historial clínico (1:1 con appointments)

Base de datos: `smartclinic_db` en PostgreSQL localhost:5432.

Notas de mapeo:
- `doctors.user_id` es `@OneToOne` con `users`
- `doctors.specialty_id` es `@ManyToOne` con `specialties`
- `doctor_schedules.doctor_id` es `@ManyToOne` con `doctors`
- `medical_records.appointment_id` es `@OneToOne` con `appointments`
- La entidad `Appointment` no usa relaciones JPA; guarda `patient_id` y `doctor_id` como `Long`, y los nombres se resuelven consultando `UserRepository`/`DoctorRepository` en el servicio.

## Comandos

```bash
# Ejecutar la aplicación
./mvnw spring-boot:run

# Compilar
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Empaquetar JAR
./mvnw clean package -DskipTests
```

## Convenciones del Proyecto

- Entidades JPA usan Lombok: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- DTOs de entrada: suffix `RequestDTO`
- DTOs de salida: suffix `ResponseDTO`
- Enums de dominio definidos como inner classes dentro de la entity
- Manejo de excepciones centralizado en `GlobalExceptionHandler`
- Naming de tablas: snake_case en BD, camelCase en Java

## Notas Importantes

1. **Seguridad abierta:** `SecurityConfig` tiene todos los endpoints como `permitAll()`. Sin autenticación real aún.
2. **`ddl-auto=update`:** Hibernate puede alterar el esquema (p.ej. cambiar tipos de columna) al arrancar.
3. **Tests unitarios:** Existen tests unitarios para los 5 services (Appointment, User, Doctor, Specialty, MedicalRecord) usando Mockito. Solo el test de contexto (`contextLoads`) conecta a la base PostgreSQL real.
4. **Swagger disponible:** Documentación OpenAPI en `/swagger-ui/index.html` cuando la app está corriendo.
