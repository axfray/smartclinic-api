# MEMORY.md — SmartClinic API
Memoria del proyecto entre sesiones. Máximo ~50 líneas: resume o elimina lo que ya no aporte.

## Estado actual
- API Spring Boot 3.2 / Java 21 con JWT + roles, PostgreSQL, Flyway y Actuator.
- Producción-ready: perfiles dev/prod/test, Docker (Dockerfile + compose) y 57 tests (unitarios + Testcontainers).
- Desplegada en Render (spec 002): `render.yaml` (web + Postgres), CI en GitHub Actions, `server.port=${PORT:8080}`, conversión `DATABASE_URL`→JDBC, rutas inexistentes→404 y Swagger no público en `prod`.

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
- Ajustar `CORS_ALLOWED_ORIGINS` cuando exista el frontend.
- Implementar `specs/001-paginacion-listados/`.
- Opcional: subir `actions/checkout` y `actions/setup-java` a v5 (CI avisa deprecación) y desactivar `spring.jpa.open-in-view`.
