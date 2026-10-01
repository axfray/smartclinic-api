# Plan 002 — Despliegue en PaaS (Render)

## Archivos y responsabilidades
- `src/main/resources/application.properties`: agregar `server.port=${PORT:8080}` (RF-1).
- `src/main/resources/application-prod.properties`: agregar `server.forward-headers-strategy=framework` para respetar `X-Forwarded-*` detrás del proxy de Render (RF-1, RF-6).
- `src/main/java/.../config/DatabaseUrlEnvironmentPostProcessor.java`: convierte `DATABASE_URL` (`postgresql://user:pass@host:port/db`, formato Render) a una URL JDBC sin credenciales, porque PostgreSQL JDBC no acepta credenciales en la URL (RF-4).
- `src/main/resources/META-INF/spring.factories`: registra el `EnvironmentPostProcessor`.
- `Dockerfile`: agregar `HEALTHCHECK` contra `/actuator/health` (RF-6).
- `.github/workflows/ci.yml`: job que corre `./mvnw test` en push y PR a `main` (RF-2, RF-3).
- `render.yaml`: blueprint con web service (Docker) + Postgres + variables de entorno (RF-4).
- `README.md`: sección "Despliegue en Render" con variables, pasos y smoke test (RF-5, RF-7).

## Decisiones técnicas (y alternativa descartada)
- Puerto por variable `PORT`. Alternativa descartada: fijarlo en el dashboard de Render (menos portable entre plataformas).
- Blueprint `render.yaml` como fuente de verdad. Alternativa descartada: configurar todo a mano en el dashboard (no reproducible).
- CI con GitHub Actions. Alternativa descartada: confiar solo en el build de Render (no corre tests).
- `JWT_SECRET` con `generateValue: true` en el blueprint (Render genera 256 bits, válido para HS256). Alternativa descartada: valor literal en el repo (filtra el secreto).
- `DATABASE_URL` de Render se traduce a JDBC en un `EnvironmentPostProcessor`. Alternativa descartada: usar `connectionString` directo (PostgreSQL JDBC lo rechaza: `UnknownHostException`). Alternativa descartada: configurar la URL a mano (rompe el IaC).
- CORS se resuelve por variable de entorno; no requiere código nuevo (RF-7).

## Estrategia de tests con ./mvnw test
- El pipeline reutiliza los tests existentes (unitarios + Testcontainers).
- Unitario nuevo: `DatabaseUrlEnvironmentPostProcessorTest` cubre la conversión `postgres://`/`postgresql://` → JDBC, puerto por defecto, query string y valores inválidos.
- Verificación local de RF-1: arrancar con `PORT=9000` y comprobar que escucha en 9000; sin `PORT`, en 8080.
- Verificación de RF-6: `curl -f http://localhost:8080/actuator/health` → 200.

## Cobertura de RF
- RF-1: `server.port=${PORT:8080}` + `forward-headers-strategy`.
- RF-2, RF-3: `.github/workflows/ci.yml`.
- RF-4: `render.yaml`.
- RF-5: sección de despliegue en `README.md`.
- RF-6: health público + Swagger off en `prod` (ya existente) + `HEALTHCHECK`.
- RF-7: `CORS_ALLOWED_ORIGINS` (ya existente, documentado).
