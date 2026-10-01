# Tareas 002 — Despliegue en PaaS (Render)

- [x] **T1. Puerto dinámico y forward-headers.** RF-1, RF-6
- Hecho cuando: con `PORT=9000` la app arranca en 9000 y sin `PORT` en 8080; `prod` respeta `X-Forwarded-*`.
- [x] **T1b. Conversión de `DATABASE_URL` a JDBC.** RF-4
- Hecho cuando: `DatabaseUrlEnvironmentPostProcessorTest` en verde y `prod` arranca usando `DATABASE_URL` (sin credenciales en la URL).
- [x] **T2. Workflow de integración continua.** RF-2, RF-3
- Hecho cuando: `.github/workflows/ci.yml` corre `./mvnw test` en push/PR a `main` y falla si los tests fallan.
- [x] **T3. Blueprint `render.yaml`.** RF-4
- Hecho cuando: define web service Docker + Postgres + variables, sin secretos versionados.
- [x] **T4. Documentación de despliegue.** RF-5, RF-7
- Hecho cuando: el README explica variables, pasos y smoke test (`/actuator/health` y login).
- [x] **T6. Ruta inexistente pública devuelve 404.** RF-8
- Hecho cuando: `GlobalExceptionHandler` mapea `NoResourceFoundException` a 404 y el test de integración pasa.
- [x] **T7. Swagger no público en prod.** RF-6
- Hecho cuando: `SecurityConfig` permite las rutas de Swagger solo con `springdoc.swagger-ui.enabled=true`.
- [x] **T5. Validación end-to-end.** RF-1..RF-8
- Hecho cuando: `./mvnw test` en verde y health 200; en Render, health 200, login 200 y Swagger 401/404 en `prod`.
- Validado: 57 tests verdes; en Render `api-docs=401`, `auth-inexistente=404`, `health=200`, `specialties` sin token `401` y login `200` con token.
