# 🏥 SmartClinic API

API RESTful desarrollada con **Spring Boot** y **PostgreSQL** para la gestión integral de turnos médicos y sincronización de pacientes y profesionales.

---

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 21
* **Framework:** Spring Boot 3.2 (Spring Data JPA, Spring Security, Spring Web)
* **Seguridad:** JWT (jjwt) + Spring Security con roles
* **Base de Datos:** PostgreSQL
* **Migraciones:** Flyway
* **Observabilidad:** Spring Boot Actuator (`/actuator/health`, `/actuator/info`)
* **Documentación:** OpenAPI / Swagger UI (`/swagger-ui/index.html`)
* **Build Tool:** Maven
* **Contenedores:** Docker + Docker Compose
* **Tests:** JUnit 5 + Mockito + Testcontainers (PostgreSQL real)

---

## 🔐 Autenticación (JWT)

La API está protegida por JWT. Todos los endpoints de `/api/**` requieren un token válido, salvo `/api/auth/**` (login), Swagger y `/actuator/health`.

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

Credenciales inválidas → `401 Unauthorized`. Usuario desactivado → `403 Forbidden`.

### Uso del token

Enviar el token en el header `Authorization` de cada request:

```
Authorization: Bearer <token>
```

### Roles

* `ROLE_ADMIN` — administra usuarios, médicos, especialidades y estados de turnos.
* `ROLE_DOCTOR` — agenda horarios, registra historial clínico y cambia estados de turnos.
* `ROLE_PATIENT` — agenda turnos, cancela los propios y consulta su historial.

### Usuario administrador inicial

Al arrancar, si no existe un usuario con el email configurado, `DataSeeder` crea un admin. En el perfil `dev` hay valores por defecto; en `prod` es obligatorio definir las variables:

| Variable | Default (solo dev) | Descripción |
| :--- | :--- | :--- |
| `ADMIN_EMAIL` | `admin@smartclinic.local` | Email del admin inicial |
| `ADMIN_PASSWORD` | `admin123` | Contraseña del admin inicial (cambiar en producción) |

---

## 📌 Endpoints Principales

### 🔑 Autenticación (`/api/auth`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Inicia sesión y devuelve un JWT | `200` / `400` / `401` |
| **GET** | `/api/auth/me` | Datos del usuario autenticado (id, rol, `doctorId`) | `200` / `401` |

### 📅 Turnos (`/api/appointments`)

| Método | Endpoint | Descripción | Rol | Estado HTTP |
| :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/appointments` | Agenda un turno (usa el paciente del token) | Paciente | `201` / `400` / `404` / `409` |
| **GET** | `/api/appointments` | Lista todos los turnos | Admin | `200` / `403` |
| **GET** | `/api/appointments/patient/{patientId}` | Turnos del propio paciente | Paciente | `200` / `403` |
| **GET** | `/api/appointments/doctor/{doctorId}` | Agenda del médico | Doctor dueño / Admin | `200` / `403` |
| **PATCH** | `/api/appointments/{id}/status` | Cambia el estado de un turno | Doctor/Admin | `200` / `400` / `404` / `409` |
| **PATCH** | `/api/appointments/{id}/cancel` | Cancela el propio turno | Paciente | `200` / `403` / `404` |

> **Seguridad:** en `POST /api/appointments` el `patientId` se toma del token, no del body. Un paciente no puede consultar ni cancelar turnos de otro paciente.

#### Ejemplo de Cuerpo de Solicitud (`POST /api/appointments`)
```json
{
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
| **POST** | `/api/users` | Crea un usuario | `201` / `409` |
| **GET** | `/api/users` | Lista usuarios | `200` |
| **GET** | `/api/users/{id}` | Usuario por id | `200` / `404` |
| **PUT** | `/api/users/{id}` | Actualiza usuario | `200` / `404` / `409` |
| **DELETE** | `/api/users/{id}` | Elimina usuario | `204` / `404` |

### 🩺 Médicos (`/api/doctors`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/doctors` | Crea un médico (Admin) | `201` / `404` / `409` |
| **GET** | `/api/doctors` | Lista médicos | `200` |
| **GET** | `/api/doctors/{id}` | Médico por id | `200` / `404` |
| **POST** | `/api/doctors/schedules` | Agrega horario a un médico (Admin) | `201` / `400` / `404` |
| **GET** | `/api/doctors/{id}/schedules` | Horarios de un médico | `200` |

### 🏷️ Especialidades (`/api/specialties`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/specialties` | Crea una especialidad (Admin) | `201` / `409` |
| **GET** | `/api/specialties` | Lista especialidades | `200` |
| **GET** | `/api/specialties/{id}` | Especialidad por id | `200` / `404` |
| **DELETE** | `/api/specialties/{id}` | Elimina especialidad (Admin) | `204` / `404` / `409` |

### 📋 Historial Clínico (`/api/medical-records`)

| Método | Endpoint | Descripción | Estado HTTP |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/medical-records` | Crea un registro clínico (Doctor) | `201` / `404` / `409` |
| **GET** | `/api/medical-records` | Lista registros clínicos (Doctor/Admin) | `200` |
| **GET** | `/api/medical-records/appointment/{appointmentId}` | Registro por turno (staff o dueño) | `200` / `403` / `404` |

---

## 🖥️ Interfaz web

La API sirve una interfaz web mínima (JS vanilla, mismo origen) desde `/`:

* **Paciente:** login, listar/agendar/cancelar sus turnos.
* **Médico:** agenda, cambio de estado de turnos e historial clínico.
* **Admin:** usuarios, especialidades, médicos, horarios y todos los turnos.

No requiere CORS (mismo origen) ni build de frontend. En producción queda en `https://<tu-servicio>.onrender.com/`.

---

## ⚠️ Manejo de Errores

Todas las respuestas de error usan un JSON estándar:

```json
{
  "timestamp": "2026-09-30T23:15:16",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado con id: 99"
}
```

| Código | Cuándo |
| :--- | :--- |
| `400` | Datos de entrada inválidos (validación, JSON malformado) |
| `401` | Token ausente/inválido o credenciales incorrectas |
| `403` | Autenticado sin permisos (rol o recurso ajeno) |
| `404` | Recurso inexistente |
| `409` | Conflicto (duplicados, doble reserva, regla de negocio) |
| `500` | Error inesperado |

---

## ⚙️ Perfiles de Configuración

* `application.properties` — configuración común (JPA, Actuator).
* `application-dev.properties` — desarrollo local (defaults cómodos).
* `application-prod.properties` — producción (sin defaults en secretos: si falta una variable, **no arranca**).
* `application-test.properties` — tests de integración (BD provista por Testcontainers).

Se elige con `SPRING_PROFILES_ACTIVE` (por defecto `dev`).

| Variable | Descripción |
| :--- | :--- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a PostgreSQL |
| `JWT_SECRET` | Clave HS256 (mínimo 32 bytes) |
| `JWT_EXPIRATION_MS` | Duración del token (default 1 h en prod) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Admin inicial |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (separados por coma) |

---

## 🚀 Puesta en Marcha

### Local (perfil `dev`)

```bash
export DB_URL=jdbc:postgresql://localhost:5432/smartclinic_db
export DB_USERNAME=postgres
export DB_PASSWORD=tu_password
./mvnw spring-boot:run
```

### Docker (perfil `prod`)

1. Copiá `.env.example` a `.env` y completá los valores (generá `JWT_SECRET` con `openssl rand -base64 48`).
2. Levantá:

```bash
docker compose up -d --build
docker compose ps
curl http://localhost:8080/actuator/health
```

Flyway crea el esquema en el primer arranque. Los datos persisten en el volumen `pgdata`.

### Render (PaaS)

El repo incluye `render.yaml` (infraestructura como código) que crea el web service y una base PostgreSQL administrada.

1. En Render: **New → Blueprint** y conectá el repositorio (rama `main`).
2. Render pedirá los secretos marcados con `sync: false`: `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
3. Ajustá `CORS_ALLOWED_ORIGINS` en el dashboard al dominio del frontend (por defecto `https://example.com`).
4. Primer deploy: Flyway crea el esquema y `DataSeeder` crea el admin.

Verificación:

```bash
curl -f https://<tu-servicio>.onrender.com/actuator/health
curl -X POST https://<tu-servicio>.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"<ADMIN_EMAIL>","password":"<ADMIN_PASSWORD>"}'
```

> Render entrega la base como `postgresql://...`; la app la convierte a JDBC usando `DATABASE_URL`. Si preferís pasar la URL completa, definí `DB_URL` con el prefijo `jdbc:`.

**Plan free:** el web service se duerme tras 15 min de inactividad (despierta en ~1 min) y la base de datos expira a los 30 días. Para producción real, subí ambos a un plan pago.

---

## 🧪 Tests

```bash
./mvnw test
```

* Tests unitarios (Mockito) de services y JwtUtil.
* Tests de integración con **Testcontainers** (levantan un PostgreSQL efímero) para seguridad: `401` sin token, `401` credenciales malas, `403` por rol, `403` IDOR y `/actuator/health` público.

> Requiere Docker corriendo. En algunos entornos con Docker muy nuevo, el `pom.xml` fija `api.version=1.43` para surefire.

---

## 📖 Documentación Interactiva

Con la app corriendo:

* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* Especificación JSON: `http://localhost:8080/v3/api-docs`

> Swagger se deshabilita automáticamente en el perfil `prod`.
