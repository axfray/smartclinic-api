# 🏥 SmartClinic API

API RESTful desarrollada con **Spring Boot** y **PostgreSQL** para la gestión integral de turnos médicos y sincronización de pacientes y profesionales.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3 (Spring Data JPA, Spring Security, Spring Web)
* **Seguridad:** JWT (jjwt) + Spring Security con roles
* **Base de Datos:** PostgreSQL
* **Documentación:** OpenAPI / Swagger UI (`/swagger-ui/index.html`)
* **Build Tool:** Maven

---

## 🔐 Autenticación (JWT)

La API está protegida por JWT. Todos los endpoints de `/api/**` requieren un token válido, salvo `/api/auth/**` (login) y Swagger.

### Login

`POST /api/auth/login` (público)

```json
{
  "email": "admin@smartclinic.local",
  "password": "admin123"
}
```

Respuesta `200 OK`:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@smartclinic.local",
  "role": "ROLE_ADMIN",
  "firstName": "Admin",
  "lastName": "SmartClinic"
}
```

### Uso del token

Enviar el token en el header `Authorization` de cada request:

```
Authorization: Bearer <token>
```

### Roles

* `ROLE_ADMIN` — administra usuarios, médicos, especialidades y estados de turnos.
* `ROLE_DOCTOR` — agenda horarios, registra historial clínico y cambia estados de turnos.
* `ROLE_PATIENT` — agenda turnos y consulta sus propios turnos.

### Usuario administrador inicial

Al arrancar, si no existe un usuario con el email configurado, `DataSeeder` crea un admin por defecto con valores configurables por variables de entorno:

| Variable | Default | Descripción |
| :--- | :--- | :--- |
| `ADMIN_EMAIL` | `admin@smartclinic.local` | Email del admin inicial |
| `ADMIN_PASSWORD` | `admin123` | Contraseña del admin inicial (cambiar en producción) |

---

## 📌 Endpoints Principales

### 🔑 Autenticación (`/api/auth`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Inicia sesión y devuelve un JWT | `200 OK` / `400 Bad Request` |

### 📅 Turnos (`/api/appointments`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/appointments` | Agenda un nuevo turno médico (Paciente) | `201 Created` / `400 Bad Request` / `409 Conflict` |
| **GET** | `/api/appointments/patient/{patientId}` | Lista los turnos asignados a un paciente (Paciente) | `200 OK` |
| **PATCH** | `/api/appointments/{id}/status` | Cambia el estado de un turno (Doctor/Admin) | `200 OK` / `400 Bad Request` / `409 Conflict` |

#### Ejemplo de Cuerpo de Solicitud (`POST /api/appointments`)
```json
{
  "patientId": 8,
  "doctorId": 7,
  "appointmentDate": "2026-09-01T10:30:00",
  "reason": "Consulta general"
}
```

#### Cambiar estado de un turno (`PATCH /api/appointments/{id}/status`)
```json
{
  "status": "CONFIRMED"
}
```
Estados válidos: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`.

**Validaciones de agendamiento:** la fecha debe ser futura, el paciente y el médico deben existir, el médico debe tener disponibilidad ese día/horario (`doctor_schedules`) y no estar ocupado en esa franja.

### 👤 Usuarios (`/api/users`) — solo Admin

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/users` | Crea un usuario | `201 Created` |
| **GET** | `/api/users` | Lista usuarios | `200 OK` |
| **GET** | `/api/users/{id}` | Usuario por id | `200 OK` |
| **PUT** | `/api/users/{id}` | Actualiza usuario | `200 OK` |
| **DELETE** | `/api/users/{id}` | Elimina usuario | `204 No Content` |

### 🩺 Médicos (`/api/doctors`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/doctors` | Crea un médico (Admin) | `201 Created` |
| **GET** | `/api/doctors` | Lista médicos | `200 OK` |
| **GET** | `/api/doctors/{id}` | Médico por id | `200 OK` |
| **POST** | `/api/doctors/schedules` | Agrega horario a un médico (Admin) | `201 Created` |
| **GET** | `/api/doctors/{id}/schedules` | Horarios de un médico | `200 OK` |

### 🏷️ Especialidades (`/api/specialties`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/specialties` | Crea una especialidad (Admin) | `201 Created` |
| **GET** | `/api/specialties` | Lista especialidades | `200 OK` |
| **GET** | `/api/specialties/{id}` | Especialidad por id | `200 OK` |
| **DELETE** | `/api/specialties/{id}` | Elimina especialidad (Admin) | `204 No Content` |

### 📋 Historial Clínico (`/api/medical-records`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/medical-records` | Crea un registro clínico (Doctor) | `201 Created` |
| **GET** | `/api/medical-records` | Lista registros clínicos (Doctor/Admin) | `200 OK` |
| **GET** | `/api/medical-records/appointment/{appointmentId}` | Registro por turno | `200 OK` |

---

## 📖 Documentación Interactiva

Con la aplicación corriendo, la documentación OpenAPI está disponible en:

* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* Especificación JSON: `http://localhost:8080/v3/api-docs`

---

## 🚀 Puesta en Marcha

```bash
# Variables de entorno para la base de datos
export DB_URL=jdbc:postgresql://localhost:5432/smartclinic_db
export DB_USERNAME=postgres
export DB_PASSWORD=tu_password

# (Opcional) Credenciales del admin inicial
export ADMIN_EMAIL=admin@smartclinic.local
export ADMIN_PASSWORD=admin123

# Ejecutar
./mvnw spring-boot:run
```