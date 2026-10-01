# MEMORY.md — SmartClinic API
Memoria del proyecto entre sesiones. Máximo ~50 líneas: resume o elimina lo que ya no aporte.

## Estado actual
- API Spring Boot 3.2 / Java 21 con JWT + roles, PostgreSQL, Flyway y Actuator.
- Producción-ready: perfiles dev/prod/test, Docker (Dockerfile + compose) y 56 tests (unitarios + Testcontainers).
- Deploy PaaS preparado (spec 002): `render.yaml` (web + Postgres), CI en GitHub Actions, `server.port=${PORT:8080}` y conversión de `DATABASE_URL` a JDBC. Pendiente: crear el Blueprint en Render y mergear a `main`.

## Decisiones (y por qué)
- Esquema con Flyway + `ddl-auto=validate` (nunca `update`): control y reproducibilidad.
- `patientId` siempre desde el token, no del body (evita IDOR).
- Errores con excepciones tipadas: `ResourceNotFoundException` (404), `ConflictException` (409).
- Secretos por variables de entorno; `.env` fuera del repo.
- SDD: `docs/constitution.md` + `specs/NNN-*/` guían el desarrollo (ver skill `sdd`).

## Aprendizajes y errores a evitar
- Testcontainers de Spring Boot 3.2.3 no soporta Docker 29: subir a 1.20.4 + surefire `api.version=1.43`.
- No pegar una clase dentro de otra (nos pasó con `ConflictException`/`ResourceNotFoundException`).
- Los archivos de perfil van en `src/main/resources/`, no dentro de `db/migration/`.

## Próximos pasos
- Mergear `feat/produccion-mvp` a `main` y crear el Blueprint en Render (spec 002).
- Verificar en Render: health 200, login 200 y Swagger 404 en `prod`.
- Implementar `specs/001-paginacion-listados/`.
