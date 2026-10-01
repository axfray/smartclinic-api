# AGENTS.md - SmartClinic API

## Visión General

API RESTful para la gestión integral de turnos médicos y sincronización de pacientes y profesionales de salud. Proyecto en desarrollo activo con una base de datos PostgreSQL que define un esquema completo (6 tablas) implementado en código Java.

## Stack Tecnológico

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.2.3
- **Persistencia:** Spring Data JPA + PostgreSQL
- **Migraciones:** Flyway (`src/main/resources/db/migration`, `ddl-auto=validate`)
- **Seguridad:** Spring Security + JWT (jjwt). Acceso por roles con `@PreAuthorize` y `@EnableMethodSecurity`
- **Observabilidad:** Spring Boot Actuator (`/actuator/health`, `/actuator/info`)
- **Documentación:** SpringDoc OpenAPI (Swagger UI en `/swagger-ui/index.html`; deshabilitado en `prod`)
- **Build Tool:** Maven (wrapper incluido)
- **Librería:** Lombok
- **Contenedores:** Docker + Docker Compose
- **Tests:** JUnit 5 + Mockito + Testcontainers (PostgreSQL efímero)

## Arquitectura

Arquitectura en capas (Layered Architecture) bajo el package `com.smartclinic.api`:

```
com.smartclinic.api/
├── ApiApplication.java              # Entry point
├── config/
│   ├── SecurityConfig.java          # Configuración de Spring Security (JWT + stateless)
│   ├── BeansConfig.java             # Beans (PasswordEncoder)
│   └── DataSeeder.java              # Crea el usuario admin inicial si no existe
├── security/
│   ├── JwtUtil.java                 # Generación/validación de tokens JWT
│   ├── JwtAuthenticationFilter.java # Filtro que autentica cada request con el token
│   └── CustomUserDetailsService.java# Carga el usuario por email para Spring Security
├── controller/
│   ├── AppointmentController.java   # Turnos
│   ├── AuthController.java          # Login (POST /api/auth/login)
│   ├── UserController.java          # Usuarios
│   ├── DoctorController.java        # Médicos + horarios
│   ├── SpecialtyController.java     # Especialidades
│   └── MedicalRecordController.java # Historial clínico
├── service/
│   ├── AppointmentService.java      # Lógica de turnos
│   ├── AuthService.java             # Lógica de autenticación
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
    ├── GlobalExceptionHandler.java  # Manejo global de excepciones (400/401/403/404/409/500)
    ├── ResourceNotFoundException.java
    └── ConflictException.java
```

Recursos de configuración:
- `src/main/resources/application.properties` (común) + `application-dev/prod.properties` (perfiles) + `application-test.properties` (tests)
- `src/main/resources/db/migration/V1__init.sql`, `V2__seed_specialties.sql`
- `Dockerfile`, `docker-compose.yml`, `.env.example` (raíz)

## Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login JWT (200 / 401) |
| GET | `/actuator/health` | Health check público (200) |
| POST | `/api/appointments` | Agenda un turno (usa el paciente del token) (201) |
| GET | `/api/appointments/patient/{patientId}` | Turnos del propio paciente (200 / 403) |
| PATCH | `/api/appointments/{id}/status` | Cambia estado de un turno (Doctor/Admin) (200) |
| PATCH | `/api/appointments/{id}/cancel` | Cancela el propio turno (Paciente) (200 / 403) |
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

El esquema se versiona con Flyway (`V1__init.sql`) y define 6 tablas, todas implementadas en Java:

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
# Ejecutar la aplicación (perfil dev por defecto)
./mvnw spring-boot:run

# Compilar
./mvnw clean compile

# Ejecutar tests (los de integración requieren Docker para Testcontainers)
./mvnw test

# Empaquetar JAR
./mvnw clean package -DskipTests

# Levantar todo con Docker (perfil prod; requiere .env)
docker compose up -d --build
```

## Convenciones del Proyecto

- Entidades JPA usan Lombok: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- DTOs de entrada: suffix `RequestDTO`
- DTOs de salida: suffix `ResponseDTO`
- Enums de dominio definidos como inner classes dentro de la entity
- Manejo de excepciones centralizado en `GlobalExceptionHandler`
- Naming de tablas: snake_case en BD, camelCase en Java

## Notas Importantes

1. **Seguridad JWT:** `SecurityConfig` protege todo `/api/**` (excepto `/api/auth/**`, Swagger y `/actuator/health`) y valida tokens JWT con roles vía `@PreAuthorize`. CORS configurado desde `cors.allowed-origins`. `DataSeeder` crea el admin inicial (`ADMIN_EMAIL`/`ADMIN_PASSWORD`).
2. **Perfiles:** `dev` (defaults locales), `prod` (sin defaults en secretos → fail-fast) y `test` (Testcontainers). Se eligen con `SPRING_PROFILES_ACTIVE`.
3. **Esquema con Flyway:** `ddl-auto=validate`; los cambios de esquema se hacen con migraciones nuevas en `db/migration` (no con Hibernate).
4. **Autorización por recurso:** el `patientId` se toma del token (no del body); paciente no puede leer turnos/historial de otro (evita IDOR).
5. **Tests:** unitarios (Mockito) para services y JwtUtil + tests de integración con Testcontainers (`AuthIntegrationTest`, `AuthorizationIntegrationTest`) que requieren Docker. El `pom.xml` fija `api.version=1.43` en surefire por compatibilidad con Docker moderno.
6. **Swagger:** disponible en `/swagger-ui/index.html` en `dev`; deshabilitado en `prod`.
7. **Docker:** `Dockerfile` multi-stage + `docker-compose.yml` (api + postgres). Secretos en `.env` (no versionado).
