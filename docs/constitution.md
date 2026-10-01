# Constitución — SmartClinic API

Principios innegociables. Toda spec, plan y tarea debe cumplirlos.

1. **La spec manda**: nada se implementa si no está en la spec activa (`specs/NNN-*/`). Si falta una decisión, se para y se pregunta.
2. **Esquema solo por migraciones**: todo cambio de base de datos va en una migración Flyway nueva (`db/migration/Vn__*.sql`). Prohibido `ddl-auto=update` y editar migraciones ya aplicadas.
3. **Tests como puerta**: la lógica se prueba con `./mvnw test` (unitarios + integración con Testcontainers). No se avanza con tests en rojo.
4. **Arquitectura en capas**: controller → service → repository. DTOs para entrada/salida; nunca exponer entidades. Errores con excepciones tipadas (404 no encontrado, 409 conflicto).
5. **Seguridad y datos**: los secretos van en variables de entorno (`.env`, no versionado). Autorización por recurso: el `patientId` sale del token, nunca del body. Los datos clínicos son sensibles.
6. **Idioma y estilo**: código y nombres en inglés; documentación y mensajes de la API en español. Java 21 + Lombok, siguiendo las convenciones de `AGENTS.md`.
