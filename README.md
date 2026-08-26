# 🏥 SmartClinic API

API RESTful desarrollada con **Spring Boot** y **PostgreSQL** para la gestión integral de turnos médicos y sincronización de pacientes y profesionales.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17+
* **Framework:** Spring Boot 3 (Spring Data JPA, Spring Security, Spring Web)
* **Base de Datos:** PostgreSQL
* **Documentación:** OpenAPI / Swagger UI
* **Build Tool:** Maven

---

## 🚀 Arquitectura del Proyecto

El proyecto sigue una **Arquitectura en Capas (Layered Architecture)** para garantizar una separación clara de responsabilidades:

* `controller/`: Expone los endpoints HTTP y gestiona las respuestas de la API.
* `service/`: Contiene la lógica de negocio y las validaciones de turnos.
* `repository/`: Capa de persistencia con Spring Data JPA.
* `model/`: Entidades relacionales del dominio (`User`, `Doctor`, `Appointment`).
* `dto/`: Objetos de transferencia de datos (`AppointmentRequestDTO`, `AppointmentResponseDTO`).
* `config/`: Configuraciones de seguridad (`SecurityConfig`).

---

## 📌 Endpoints Principales

### 📅 Turnos (`/api/appointments`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/appointments` | Agenda un nuevo turno médico | `201 Created` / `400 Bad Request` |
| **GET** | `/api/appointments/patient/{patientId}` | Lista los turnos asignados a un paciente | `200 OK` |

#### Ejemplo de Cuerpo de Solicitud (`POST /api/appointments`)
```json
{
  "patientId": 8,
  "doctorId": 7,
  "appointmentDate": "2026-09-01T10:30:00",
  "reason": "Consulta general"
}