# Spec 003 — Interfaz web mínima servida por la API

Estado: aprobada

## Contexto y objetivo
La API ya está desplegada y accesible por HTTPS, pero solo se puede operar con HTTP/JSON (Postman/curl). Se necesita una interfaz web mínima, servida por la misma API (mismo origen), para que pacientes, médicos y administradores operen sin herramientas externas.

## Usuarios / actores
- Paciente: consulta, agenda y cancela sus turnos.
- Médico: consulta su agenda, cambia estados y carga historial clínico.
- Admin: gestiona usuarios, especialidades, médicos, horarios y ve todos los turnos.

## Historias de usuario
- HU-1. Como paciente, quiero iniciar sesión y ver/agendar/cancelar mis turnos.
- HU-2. Como admin, quiero gestionar usuarios, especialidades, médicos y horarios.
- HU-3. Como médico, quiero ver mi agenda, cambiar estados y cargar historial clínico.
- HU-4. Como usuario, quiero que la UI me muestre solo las acciones de mi rol.

## Definiciones
- **Mismo origen**: la UI y la API se sirven desde el mismo dominio, por lo que no se aplica CORS.

## Requisitos funcionales (EARS)
- RF-1: CUANDO un usuario inicia sesión, EL SISTEMA muestra la pantalla correspondiente a su rol.
- RF-2: EL SISTEMA sirve la interfaz desde el mismo origen que la API (sin CORS).
- RF-3: CUANDO el paciente agenda un turno, EL SISTEMA usa el paciente del token, no uno elegido por la UI.
- RF-4: EL SISTEMA expone `GET /api/auth/me` con id, email, rol, nombres y (si es médico) su `doctorId`.
- RF-5: EL SISTEMA expone el listado de todos los turnos únicamente para el rol admin.
- RF-6: EL SISTEMA expone la agenda de un médico únicamente para ese médico o un admin.
- RF-7: SI el token falta o expira, ENTONCES EL SISTEMA devuelve al usuario a la pantalla de login.
- RF-8: EL SISTEMA no muestra a un paciente datos de otros pacientes.
- RF-9: EL SISTEMA carga los archivos estáticos de la interfaz sin requerir token.

## Requisitos no funcionales
- Sin dependencias de frontend (JS vanilla, sin build).
- No alterar la seguridad existente (roles y autorización por recurso).
- La interfaz no contiene secretos.

## Casos límite
- Sesión expirada (401): se limpia el token y se vuelve al login.
- Listas vacías: se muestra un mensaje, sin error.
- Errores de validación/conflicto (400/409): se muestran al usuario.

## Fuera de alcance
- Paginación de listados (spec 001).
- Framework SPA, diseño avanzado, internacionalización.
- Tests automatizados de navegador (se verifica con curl y uso manual).

## Criterios de finalización
- `./mvnw test` en verde.
- En Render, `GET /` sirve la interfaz y el login funciona.
- Flujos por rol (paciente, médico, admin) operables desde el navegador.

## Dudas abiertas
- Ninguna.
