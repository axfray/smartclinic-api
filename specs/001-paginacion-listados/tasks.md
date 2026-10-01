# Tareas 001 — Paginación de listados

- [ ] **T1. Configurar defaults de paginación.** RF-2, RF-3
- Hecho cuando: `application.properties` define `default-page-size=20` y `max-page-size=100`.
- [ ] **T2. Paginar `GET /api/users`.** RF-1, RF-2, RF-4, RF-5
- Hecho cuando: devuelve `Page` con metadatos y `size=-1` responde 400.
- [ ] **T3. Paginar `GET /api/doctors` y `GET /api/specialties`.** RF-1, RF-2, RF-5
- Hecho cuando: ambos devuelven `Page` con metadatos.
- [ ] **T4. Paginar `GET /api/medical-records`.** RF-1, RF-2, RF-5
- Hecho cuando: devuelve `Page` con metadatos.
- [ ] **T5. Test de integración de paginación.** RF-1, RF-2, RF-3, RF-4, RF-5
- Hecho cuando: `./mvnw test` en verde con el nuevo test.
