# SPS Microsoftware

Arquitectura microservicios para compra de planes de salud.

## Componentes

- Frontend Angular SPA con Nginx
- API Gateway Spring Cloud Gateway
- Auth Service Spring Boot
- Catalog Service Spring Boot
- Purchase Service Spring Boot
- SNS Mock Spring Boot
- Salud Pay ASP.NET Core Web API
- SHC Spring Boot
- SAM Spring Boot
- MySQL 8 como única instancia de base de datos
- RabbitMQ para mensajería asíncrona

## Cómo iniciar

1. Ajusta credenciales en `.env`
2. Ejecuta:

```bash
docker-compose up --build
```

## Bases de datos lógicas

- `sps_auth_db`
- `sps_catalog_db`
- `sps_purchase_db`
- `saludpay_db`
- `shc_db`
- `sam_db`
- `sns_db`

## Siguiente paso

Completar implementaciones de los microservicios en cada carpeta con:
- `Dockerfile`
- `pom.xml` / `.csproj`
- aplicación Spring Boot / ASP.NET Core
- configuración de mensajería y seguridad JWT
- tests y documentación
