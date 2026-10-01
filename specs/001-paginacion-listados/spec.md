# Spec 001 — Paginación de listados

Estado: borrador

## Contexto y objetivo
Hoy los endpoints de listado (usuarios, médicos, especialidades, historial y turnos) devuelven todos los registros. A medida que crece la base, esto degrada el rendimiento y la experiencia. Se necesita poder pedir los listados por páginas.

## Usuarios / actores
- Admin: gestiona usuarios, médicos y especialidades.
- Doctor: consulta el historial clínico.
- Paciente: consulta sus propios turnos.

## Historias de usuario
- HU-1. Como admin, quiero pedir una página de usuarios, para no cargar todos de golpe.
- HU-2. Como doctor, quiero paginar el historial clínico, para revisarlo por partes.

## Requisitos funcionales (EARS)
- RF-1: CUANDO el cliente llama a un endpoint de listado, EL SISTEMA devuelve una página con `content`, `page`, `size`, `totalElements` y `totalPages`.
- RF-2: CUANDO el cliente envía `page` y `size`, EL SISTEMA usa esos valores (por defecto `page=0`, `size=20`).
- RF-3: SI `size` es mayor a 100, ENTONCES EL SISTEMA lo limita a 100.
- RF-4: SI `page` o `size` son negativos, ENTONCES EL SISTEMA devuelve 400.
- RF-5: EL SISTEMA mantiene el orden actual de cada listado.

## Requisitos no funcionales
- No alterar la seguridad existente (roles y autorización por recurso).

## Casos límite
- Página fuera de rango: `content` vacío, sin error.
- Listado vacío: página válida con `totalElements=0`.

## Fuera de alcance
- Filtros y búsqueda por texto (otra spec).
- Orden configurable por el cliente.

## Criterios de finalización
- Todos los RF con test en verde (`./mvnw test`) y verificación con `curl`.

## Dudas abiertas
- [NECESITA ACLARACIÓN] ¿Paginamos también `GET /api/medical-records/appointment/{id}`? (devuelve un único registro)
