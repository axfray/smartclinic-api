---
description: SDD · Genera las tareas de una spec (uso: /sdd-tasks 002-nombre)
agent: plan
---
A partir de `specs/$1/spec.md` y `specs/$1/plan.md`, genera `specs/$1/tasks.md`. Usa la skill sdd.

Tareas pequeñas (máx. 20-30 min cada una), en orden de dependencia, cada una con los RF que cubre y una línea "Hecho cuando:" verificable. Usa checkboxes.

Si salen más de 10 tareas, propón dividir la spec.
