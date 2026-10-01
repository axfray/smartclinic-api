# Spec 002 — Despliegue en PaaS (Render)

Estado: aprobada

## Contexto y objetivo
La API ya es production-ready a nivel código (perfiles dev/prod/test, Dockerfile, Flyway, JWT y tests), pero todavía no está publicada. Se necesita desplegarla en un PaaS de forma reproducible, automatizada y sin exponer secretos, para que quede accesible por HTTPS y con una base administrada.

## Usuarios / actores
- Responsable de despliegue/DevOps del equipo.

## Historias de usuario
- HU-1. Como responsable, quiero que la app escuche en el puerto que asigna la plataforma, para que el servicio quede saludable.
- HU-2. Como responsable, quiero que cada push ejecute los tests automáticamente, para no desplegar código roto.
- HU-3. Como responsable, quiero una definición de infraestructura en el repo, para desplegar sin pasos manuales ambiguos.
- HU-4. Como responsable, quiero documentación de variables y pasos, para desplegar y operar la API.

## Definiciones
- **PaaS**: plataforma que provee build, ejecución, TLS y base administrada (aquí Render).
- **Blueprint**: definición de infraestructura como código versionada en el repo.

## Requisitos funcionales (EARS)
- RF-1: EL SISTEMA escucha en el puerto indicado por la variable de entorno `PORT`, usando 8080 cuando no está definida.
- RF-2: CUANDO se hace push a la rama de integración (`main`), EL SISTEMA ejecuta `./mvnw test` en integración continua.
- RF-3: SI los tests fallan, ENTONCES el pipeline de integración continua falla y no se despliega.
- RF-4: EL SISTEMA incluye una definición de infraestructura como código que describe el servicio web y la base de datos administrada.
- RF-5: EL SISTEMA documenta las variables de entorno requeridas y los pasos de despliegue con una verificación de humo.
- RF-6: MIENTRAS el perfil `prod` está activo, EL SISTEMA expone `/actuator/health` como health check y mantiene la documentación interactiva deshabilitada.
- RF-7: EL SISTEMA acepta CORS únicamente desde los orígenes configurados en `CORS_ALLOWED_ORIGINS`.

## Requisitos no funcionales
- Ningún secreto versionado en el repositorio.
- El pipeline de integración continua completa en menos de 10 minutos.
- No se altera el flujo de desarrollo local (`dev`) ni el `docker-compose`.

## Casos límite
- `PORT` ausente: la app usa 8080 (desarrollo local).
- Sin dominio de frontend todavía: `CORS_ALLOWED_ORIGINS` acepta un valor placeholder configurable.
- Primer despliegue con base vacía: Flyway crea el esquema y el sembrador crea el admin.

## Fuera de alcance
- Rate limiting en el login.
- Métricas Prometheus y tracing.
- Backups automáticos y entornos de staging.
- Frontend.

## Criterios de finalización
- Tests en verde en local y en integración continua.
- `/actuator/health` responde 200 en el PaaS.
- El login del admin funciona en el entorno desplegado.

## Dudas abiertas
- Ninguna. Plataforma elegida: Render; rama de deploy: `main`.
