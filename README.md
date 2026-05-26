# SPS Enterprise (SaludPay System)

Arquitectura orientada a microservicios para la gestion y compra de planes de salud.

---

## 📌 Estado del Proyecto y Progreso

Actualmente, la infraestructura base y la orquestacion con `docker-compose` estan configuradas. Ya se han implementado y probado con exito los microservicios principales de autenticacion y catalogo. 

### ✅ Lo que tenemos implementado y probado:
- **Infraestructura Base:** MySQL 8 y RabbitMQ orquestados via Docker Compose.
- **Auth Service (Spring Boot):** Servicio de autenticacion completo. Genera y valida tokens JWT. (Test superado).
- **Catalog Service (Spring Boot):** Gestion de Planes de Salud (`PlanEntity`). Soporta listado (GET) y creacion (POST) de planes. (Test superado).

### ⏳ Lo que falta por completar:
- **API Gateway (Spring Cloud Gateway):** Configurar el enrutamiento y la validacion de tokens a nivel del gateway.
- **Purchase Service (Spring Boot):** Logica principal de compras de planes.
- **SNS Mock (Spring Boot):** Simulador del servicio nacional de salud.
- **Salud Pay (ASP.NET Core Web API):** Pasarela de pagos.
- **SHC Service & SAM Service (Spring Boot):** Servicios satelites.
- **Frontend (Angular SPA con Nginx):** Integracion de las pantallas con los microservicios.

---

## 🐋 Comandos Docker Compose Utiles

Aqui tienes los comandos mas comunes para gestionar la arquitectura:

- **Levantar todos los servicios en segundo plano (recompilando si hay cambios):**
  ```bash
  docker-compose up -d --build
  ```

- **Detener todos los contenedores y redes:**
  ```bash
  docker-compose down
  ```

- **Recompilar y reiniciar un servicio especifico (util al desarrollar un solo microservicio):**
  ```bash
  docker-compose build <nombre-del-servicio>
  docker-compose up -d <nombre-del-servicio>
  ```
  *(Ejemplo: `docker-compose build catalog-service && docker-compose up -d catalog-service`)*

- **Ver los logs de un servicio en tiempo real:**
  ```bash
  docker-compose logs -f <nombre-del-servicio>
  ```
  *(El parametro `-f` hace que la consola siga mostrando los logs. Usa `Ctrl+C` para salir).*

- **Ver los ultimos logs (ejemplo, ultimas 50 lineas):**
  ```bash
  docker-compose logs --tail=50 <nombre-del-servicio>
  ```

---

## 🧪 Pruebas Exitosas (Tests)

A continuacion, se documentan los comandos de PowerShell utilizados para certificar el correcto funcionamiento de los microservicios que ya estan listos.

### 1. Test del `auth-service` (Generacion de JWT)
Este servicio se ejecuta en el puerto **8081**. Este comando realiza un POST al endpoint de login para obtener un token Bearer.

```powershell
$body = @{username="admin"; password="admin123"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8081/auth/login" -Method POST -ContentType "application/json" -Body $body
```

### 2. Test del `catalog-service` (Creacion y Consulta de Planes)
Este servicio se ejecuta en el puerto **8082** y maneja los **Planes de Salud** (ruta `/planes`).

**Crear un Plan Nuevo:**
*(Asegurate de ejecutar primero el test de autenticacion para tener la variable `$token` cargada, y evita tildes para no tener problemas de codificacion UTF-8 en PowerShell)*
```powershell
$headers = @{ Authorization = "Bearer $token" }

$plan1 = @{
    nombre = "Plan Basico"
    descripcion = "Cobertura medica esencial"
    precio = 1500.00
    convenio = "Particular"
    activo = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8082/planes" -Method POST -Headers $headers -ContentType "application/json; charset=utf-8" -Body $plan1
```

**Listar todos los Planes (GET):**
```powershell
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri "http://localhost:8082/planes" -Method GET -Headers $headers
```

---

## 🗄 Bases de Datos Logicas

Cada servicio maneja su propio esquema dentro de la misma instancia de MySQL:
- `sps_auth_db`
- `sps_catalog_db`
- `sps_purchase_db`
- `saludpay_db`
- `shc_db`
- `sam_db`
- `sns_db`
