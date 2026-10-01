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
- [ ] **T5. Validación end-to-end.** RF-1..RF-7
- Hecho cuando: `./mvnw test` en verde y health 200; en Render, health 200, login 200 y Swagger 404 en `prod`.
- Nota: verificación local hecha (56 tests verdes + arranque `prod` con `DATABASE_URL`); falta el deploy real en Render.
