# Plan 003 — Interfaz web mínima servida por la API

## Archivos y responsabilidades
- `dto/MeResponseDTO.java`: id, email, rol, nombres y `doctorId` del usuario autenticado (RF-4).
- `service/AuthService.java`: agregar `getCurrentUser(email)` que resuelve `doctorId` vía `DoctorRepository.findByUserId` (RF-4).
- `controller/AuthController.java`: `GET /api/auth/me` (autenticado) (RF-4).
- `repository/AppointmentRepository.java`: agregar `findByDoctorId(Long)` (RF-6).
- `service/AppointmentService.java`: `getAllAppointments()` (RF-5) y `getAppointmentsByDoctor(doctorId, currentUserId, isStaff)` con validación de pertenencia (RF-6).
- `controller/AppointmentController.java`: `GET /api/appointments` (ADMIN) y `GET /api/appointments/doctor/{doctorId}` (DOCTOR/ADMIN) (RF-5, RF-6).
- `config/SecurityConfig.java`: permitir los estáticos (`/`, `/index.html`, `/app.js`, `/styles.css`) (RF-9).
- `src/main/resources/static/index.html`, `app.js`, `styles.css`: interfaz (RF-1, RF-2, RF-3, RF-7, RF-8).

## Decisiones técnicas (y alternativa descartada)
- JS vanilla, sin build. Alternativa descartada: SPA con React/Vue (más peso y un segundo deploy).
- Servir la UI desde el mismo jar (mismo origen). Alternativa descartada: Render Static Site (requiere CORS).
- `GET /api/auth/me` para obtener id y doctorId. Alternativa descartada: agregar `id` al `AuthResponseDTO` (no resuelve `doctorId`).
- El listado admin y la agenda del médico se agregan a los controllers existentes. Alternativa descartada: nuevos controllers (duplicación).

## Estrategia de tests con ./mvnw test
- Unitarios: `AuthServiceTest.getCurrentUser_*` (con `DoctorRepository` mockeado) y `AppointmentServiceTest` para `getAllAppointments` y la autorización de `getAppointmentsByDoctor` (médico dueño vs ajeno).
- Integración: `AuthorizationIntegrationTest` — `GET /api/auth/me` autenticado 200; `GET /api/appointments` como paciente 403; sin token 401.

## Cobertura de RF
- RF-1, RF-7, RF-8: `app.js` (render por rol, manejo de 401, uso del id del token).
- RF-2, RF-9: estáticos en el mismo jar + `SecurityConfig`.
- RF-3: la UI no envía `patientId` como dueño; el backend lo toma del token (ya existente).
- RF-4: `/api/auth/me`.
- RF-5: `GET /api/appointments`.
- RF-6: `GET /api/appointments/doctor/{doctorId}`.
