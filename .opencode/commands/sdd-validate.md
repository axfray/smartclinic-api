---
description: SDD · Valida una spec implementada (uso: /sdd-validate 002-nombre)
agent: build
---
Recorre `specs/$1/spec.md` requisito por requisito. Para cada RF indica qué test lo cubre y el resultado de ejecutarlo (`./mvnw test`).

Los RF que no se puedan testear automáticamente (p. ej. endpoints), verifícalos con `curl` contra la API.

Si algún RF no está cubierto o falla, dilo claramente. Después comprueba los criterios de finalización y dame un veredicto: ¿la spec está cumplida?
