# Plan 001 — Paginación de listados

## Archivos y responsabilidades
- `*Repository` (User, Doctor, Specialty, MedicalRecord, Appointment): ya exponen `findAll(Pageable)` por heredar de `JpaRepository`. No requieren cambios.
- `*Service`: cambiar los `getAll*` para aceptar `Pageable` y devolver `Page<*ResponseDTO>`.
- `*Controller`: aceptar `Pageable` (Spring lo resuelve desde query params) y devolver `Page<*ResponseDTO>`.
- `application.properties`: fijar `spring.data.web.pageable.default-page-size=20` y `max-page-size=100`.

## Decisiones técnicas (y alternativa descartada)
- Usar `Pageable`/`Page` de Spring Data. Alternativa descartada: `limit`/`offset` manual (más código y más errores).
- Devolver `Page<T>` (trae los metadatos de RF-1). Alternativa descartada: envoltorio propio (innecesario).

## Estrategia de tests con ./mvnw test
- Unitarios: verificar que los services delegan en el repository con el `Pageable` recibido.
- Integración: llamar a un endpoint de listado con `?page=0&size=2` y comprobar metadatos, y con `size=-1` esperar 400.

## Cobertura de RF
- RF-1, RF-2: `Pageable` + `Page`.
- RF-3: `max-page-size=100`.
- RF-4: validación de `page`/`size` negativos → 400.
- RF-5: `findAll(Pageable)` sin `Sort` extra.
