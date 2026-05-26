# SPS — Sistema de Planes de Salud

Proyecto Arquisoft · Universidad Javeriana  
Arquitectura de Microservicios: Java EE (Spring Boot) + .NET

---

## Servicios y Puertos

| Servicio               | Tecnologia        | Puerto | Red            |
|------------------------|-------------------|--------|----------------|
| Angular Frontend       | Nginx + Angular   | 4200   | publica        |
| API Gateway            | Spring Cloud GW   | 8080   | publica        |
| Auth Service           | Spring Boot       | 8081   | privada        |
| Catalog Service        | Spring Boot       | 8082   | privada        |
| Purchase Service (×2)  | Spring Boot       | 8083   | privada        |
| SNS Mock               | Spring Boot       | 8085   | privada        |
| SaludPay (.NET)        | ASP.NET Core      | 8086   | privada        |
| SHC Service            | Spring Boot       | 8087   | privada        |
| SAM Service            | Spring Boot       | 8088   | privada        |
| MySQL                  | MySQL 8.0         | 3306   | privada        |
| RabbitMQ               | RabbitMQ 3        | 5672   | privada        |
| RabbitMQ Management    | -                 | 15672  | privada        |

---

## Credenciales de Prueba

### SPS (Spring Boot Auth)
- Usuario: `admin` / Contraseña: `admin123`

### SaludPay (.NET)
- Cedula: `1001` / Contraseña: `password123`
- Cedula: `1002` / Contraseña: `password123`

---

## Flujo Principal de Compra

```
1. Cliente hace login → GET /auth/login → JWT Token
2. Consulta catalogo → GET /catalog/planes → Lista de planes con servicios medicos
3. Inicia compra → POST /purchase/compras { clienteId, cedula, planIds, total }
4. [SNS valida asincronamente] → webhook POST /purchase/compras/webhook-sns
5. Si APROBADO → Purchase envia pago pendiente a SaludPay
6. Usuario se autentica en SaludPay (cedula + password) → JWT
7. Consulta pagos → GET /api/pagos/mis-pagos?cedula=1001
8. Ejecuta pago → POST /api/pagos/{id}/pagar
9. SaludPay notifica a Purchase → POST /purchase/compras/webhook-pago { compraId, estado }
10. Purchase publica evento RabbitMQ → SHC y SAM registran historia clinica y agenda
11. Email de confirmacion al cliente (simulado en logs)
```

---

## Catalogo de Planes

| Plan          | Servicios Incluidos                                          | Precio      |
|---------------|--------------------------------------------------------------|-------------|
| Plan Basico   | Consulta General, Examenes Lab, Hospitalizacion Basica       | $129.900    |
| Plan Avanzado | Consulta Especialista, Examenes Avanzados, Hosp. Especializada | $279.900  |
| Plan Familiar | Cobertura Medica Familiar (hasta 4 miembros)                 | $399.900    |

---

## Endpoints Clave

### Gateway → Auth
```
POST http://localhost:8080/auth/login
{ "username": "admin", "password": "admin123" }
```

### Gateway → Catalog
```
GET http://localhost:8080/catalog/planes
Authorization: Bearer <token>
```

### Gateway → Purchase
```
POST http://localhost:8080/purchase/compras
{ "clienteId": 1, "cedula": "1001", "planIds": [1], "total": 129900 }

GET  http://localhost:8080/purchase/compras/{id}
POST http://localhost:8080/purchase/compras/webhook-sns
POST http://localhost:8080/purchase/compras/webhook-pago
```

### SaludPay (.NET)
```
POST http://localhost:8086/api/auth/login
{ "cedula": "1001", "password": "password123" }

GET  http://localhost:8086/api/pagos/mis-pagos?cedula=1001
POST http://localhost:8086/api/pagos/{id}/pagar
```

---

## Despliegue

```bash
# Levantar todo el sistema
docker compose up -d

# Reconstruir un servicio
docker compose build <service-name>
docker compose up -d --no-deps <service-name>

# Ver logs
docker logs <container-name> -f

# Estado
docker ps
```

---

## Separacion de Redes

- **sps-public-net**: Frontend Angular + API Gateway
- **sps-private-net**: Todos los microservicios, DBs y mensajeria

El frontend solo puede hablar con el Gateway. Ningun servicio interno es accesible directamente desde el exterior.

---

## Bases de Datos Independientes

| Servicio         | Base de Datos       |
|------------------|---------------------|
| Auth Service     | `sps_auth_db`       |
| Catalog Service  | `sps_catalog_db`    |
| Purchase Service | `sps_purchase_db`   |
| SaludPay         | `saludpay_db`       |
| SHC Service      | `shc_db`            |
| SAM Service      | `sam_db`            |
| SNS Mock         | `sns_db`            |
