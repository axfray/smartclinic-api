---
description: SDD · Genera el plan técnico de una spec (uso: /sdd-plan 002-nombre)
agent: plan
---
Lee `docs/constitution.md` y `specs/$1/spec.md`. NO escribas código. Usa la skill sdd.

Genera `specs/$1/plan.md` con:
- Archivos que se crean o modifican y la responsabilidad de cada uno.
- Servicios/métodos necesarios y su responsabilidad.
- Algoritmo en pseudocódigo (si aplica).
- Cómo se expone en la API (endpoints y DTOs).
- Decisiones técnicas justificadas (y su alternativa descartada).
- Estrategia de tests con `./mvnw test`.

Indica qué RF cubre cada parte. Todo debe respetar la constitución.
