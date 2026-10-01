# MEMORY.md — SmartClinic API
Memoria del proyecto entre sesiones. Máximo ~50 líneas: resume o elimina lo que ya no aporte.

## Estado actual
- API Spring Boot 3.2 / Java 21 con JWT + roles, PostgreSQL, Flyway y Actuator.
- Producción-ready: perfiles dev/prod/test, Docker (Dockerfile + compose) y 52 tests (unitarios + Testcontainers).
- Corre en Docker local (api healthy). Pendiente: deploy a VPS (Nginx + HTTPS + backups) y CI.

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
- Deploy a VPS (Fase 5: Nginx + HTTPS + backups).
- CI con GitHub Actions que corra `./mvnw test`.
- Implementar la primera spec SDD: `specs/001-paginacion-listados/`.
