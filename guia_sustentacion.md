# Guía de Estudio y Arquitectura para Sustentación del Proyecto SPS

Este documento detalla el paso a paso técnico del proyecto para la sustentación frente al docente. Describe los flujos del sistema correlacionándolos directamente con las clases, métodos y archivos de código reales del proyecto, el esquema de infraestructura Docker y comandos esenciales de monitoreo.

---

## 📐 Resumen Arquitectónico del Sistema

El sistema implementa una arquitectura orientada a microservicios distribuidos, desplegada localmente mediante **Docker**.
- **Frontend SPA**: Angular 17, servido por un contenedor **Nginx** local.
- **API Gateway**: Spring Cloud Gateway para centralizar y enrutar las peticiones seguras de forma balanceada.
- **Microservicios Backend**:
  - [auth-service](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/auth-service) (Java 17 / Spring Boot): Registro e inicio de sesión con JWT.
  - [catalog-service](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/catalog-service) (Java 17 / Spring Boot): Consulta y mantenimiento de planes médicos y sus servicios asociados.
  - [purchase-service](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service) (Java 17 / Spring Boot): Orquestación de compras, integración de pagos y validación SNS.
  - [sns-mock](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/sns-mock) (Java 17 / Spring Boot): Simulación asíncrona de validación ante la Superintendencia Nacional de Salud.
  - [saludpay-dotnet](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet) (C# / .NET 8): Pasarela de pagos independiente que expone endpoints REST y se comunica de vuelta al MS de compra vía webhook HTTP.
  - [shc-service](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/shc-service) (Java 17 / Spring Boot): Sistema de Historia Clínica. Consume eventos asíncronos de RabbitMQ para registrar la compra.
  - [sam-service](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/sam-service) (Java 17 / Spring Boot): Sistema de Agenda Médica. Consume eventos asíncronos de RabbitMQ para programar y bloquear agendas de doctores de acuerdo con los servicios adquiridos.
- **Base de Datos**: MySQL 8.0 con 7 esquemas independientes separados para aislar los microservicios.
- **Broker de Mensajería**: RabbitMQ 3-management para comunicación asíncrona basada en eventos utilizando la cola `shc.queue` y `sam.queue`.

---

## 🔄 Detalle Paso a Paso de los 5 Flujos de Negocio en el Código

*(Nota: Los "timers/cron-jobs" se omitieron en la explicación según lo acordado, ya que la comunicación se ejecuta mediante flujo reactivo WebFlux y eventos asíncronos por colas RabbitMQ en tiempo real).*

### 🛠️ Flujo 1: Carrito, Autenticación y Creación de Petición de Compra
**Objetivo:** El cliente busca y añade planes al carrito, inicia sesión con usuario y contraseña, y envía la petición de compra que se guarda en la base de datos de compras.

1. **Interfaz Angular**:
   - El cliente selecciona los planes en el carrito y hace clic en "Comprar".
   - Si no está autenticado, la SPA redirige a la vista de Login. El formulario envía las credenciales mediante un POST al endpoint de autenticación.
   - **Ruta de red**: Angular SPA (puerto `4200`) -> Nginx -> API Gateway (`http://api-gateway:8080/api/auth/login`) -> `auth-service` (puerto `8081`).
   - `auth-service` valida las credenciales y devuelve un **Token JWT** que se almacena en el cliente.
2. **Envío de Compra**:
   - Con el token JWT, la SPA Angular envía la petición de compra (un array de `planIds` y el `clienteId`).
   - El Nginx ([nginx.conf](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/angular-frontend/nginx.conf#L12)) captura la petición bajo `/api/` y la redirige al API Gateway:
     ```nginx
     location /api/ {
         proxy_pass http://api-gateway:8080/;
     }
     ```
   - El API Gateway redirecciona el tráfico al microservicio de compras (`http://purchase-service:8083/compras`) usando balanceo de carga.
3. **Guardado en Base de Datos**:
   - En `purchase-service`, la petición ingresa a [PurchaseController.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/controller/PurchaseController.java#L23) en el método `createPurchase`:
     ```java
     @PostMapping
     public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request)
     ```
   - El controlador delega a [PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L62) en el método `createPurchase`.
   - **Regla de Negocio en Backend**: Para evitar fraude en precios desde el frontend, el backend calcula el total llamando al microservicio de catálogo mediante el método `calculateTotalFromCatalog(request.getPlanIds())` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L86)).
   - Se crea la entidad `PurchaseEntity` con estado inicial `VALIDANDO_SNS` y se guarda en la base de datos `sps_purchase_db` a través de `purchaseRepository.save(purchase)` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L76)).
4. **Mensaje en Pantalla**:
   - La petición es síncrona en el HTTP request/response y devuelve un `PurchaseResponse` indicando que el proceso de validación ha comenzado.
   - El frontend Angular le muestra un mensaje en pantalla al cliente informándole que se validará su estado y que **le llegará un correo electrónico** cuando sea aprobado para continuar la compra.

---

### 📬 Flujo 2: Validación Asíncrona con SNS (Spring WebFlux y Circuit Breaker)
**Objetivo:** El sistema de compra se comunica asíncronamente con el validador de la Superintendencia Nacional de Salud (SNS) para verificar que el usuario no tenga impedimentos de afiliación.

1. **Disparador Asíncrono**:
   - Inmediatamente después de guardar la compra en el Flujo 1, `createPurchase` inicia la validación llamando a `invokeSnsValidationAsync(purchase)` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L77)).
   - Este método llama a `invokeSnsValidation(purchase)` el cual ejecuta una cadena reactiva sin bloquear el hilo principal.
2. **Petición con Spring WebFlux (WebClient)**:
   - Se utiliza `WebClient` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L194)) para enviar una petición HTTP POST asíncrona al simulador del SNS (`sns-mock`) en la URL `http://sns-mock:8085/sns/validar`:
     ```java
     return webClient.post()
             .uri(snsServiceUrl + "/sns/validar")
             .bodyValue(Map.of(...))
     ```
   - La llamada está decorada con un **Circuit Breaker** (`@CircuitBreaker(name = "purchaseCircuitBreaker")`) para evitar caídas del sistema en caso de que el validador SNS no esté operativo. Si falla, el método `fallbackSnsValidation` maneja el error marcando el estado como `ERROR_SNS`.
3. **Procesamiento de Respuesta Reactiva**:
   - La respuesta no bloquea el Event Loop de Spring. La cadena reactiva de WebFlux recibe el resultado y cambia de contexto de hilos para operaciones bloqueantes de base de datos usando `.publishOn(Schedulers.boundedElastic())` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L206)).
   - Invoca el método `processSnsResult(purchase.getId(), resultado)` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L236)) para guardar el veredicto (`APROBADO`, `RECHAZADO`, `ENPROCESO`) en la base de datos `sps_purchase_db`.

---

### 📧 Flujo 3: Notificación de Aprobación
**Objetivo:** Informar al cliente que la validación ante el SNS fue satisfactoria y que puede realizar el pago.

1. **Actualización de Estado**:
   - En el método `processSnsResult` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L236)), si el resultado devuelto reactivamente por el SNS es `"APROBADO"`, se realiza lo siguiente:
     - El estado de la compra cambia a `PENDIENTE_PAGO` en la base de datos de compras.
     - Se invoca `notifySaludPay(purchase)` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L162)) para notificar e insertar el pago pendiente en el sistema independiente SaludPay.
2. **Envío de Correo Electrónico**:
   - Inmediatamente, se llama a `notificationService.sendPaymentNotification(purchase.getClienteId(), purchase.getId(), purchase.getTotal())` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L244)).
   - El método `sendPaymentNotification` ([NotificationService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/NotificationService.java#L14)) registra un log simulando el correo con el link específico de pago directo:
     ```
     📧 EMAIL ENVIADO → Destinatario: cliente_1@sps-salud.com | Asunto: Orden de Pago Pendiente | Valor a pagar: $129900.00 | URL de Pago: http://localhost:4200/saludpay?id=1
     ```

---

### 💳 Flujo 4: Consulta de Pagos y Transacción en SaludPay
**Objetivo:** El cliente inicia sesión en la aplicación de SaludPay, consulta sus facturas pendientes y realiza el pago. Este pago se comunica asíncronamente al microservicio de compras para actualizar su estado.

1. **Ingreso con Validación Cruzada de Credenciales**:
   - El cliente ingresa a la sección SaludPay en Angular e introduce su cédula y contraseña del SPS.
   - Angular envía las credenciales al endpoint `/api/saludpay/auth/login` del C# microservice ([PagosController.cs](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet/Controllers/PagosController.cs)).
   - El [AuthController.cs](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet/Controllers/AuthController.cs#L24) de C# **re-envía las credenciales al microservicio Java `auth-service`** en `/auth/validate-cedula` usando un `HttpClient` inyectado.
   - El nuevo endpoint [AuthController.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/auth-service/src/main/java/com/sps/auth/controller/AuthController.java#L47) en Java busca al usuario por cédula vía `userRepository.findByCedula(cedula)` y verifica la contraseña con `BCrypt` (`passwordEncoder.matches`).
   - **Integración real**: Si el usuario existe en `sps_auth_db` y la contraseña es correcta, `auth-service` responde 200 OK a SaludPay, que a su vez responde 200 OK con un token al frontend.
   - **Tolerancia a fallos (Graceful Degradation)**: Si `auth-service` no responde, `AuthController.cs` tiene un bloque `catch` que hace fallback a la base de datos local `saludpay_db.saludpay_users` para verificar las credenciales sincrónicamente. Demo de acceso: Cédula `1001`, contraseña `password123`.
2. **Consulta de Pagos Pendientes**:
   - Con el token obtenido en el login, Angular hace una llamada GET a `/api/saludpay/pagos/mis-pagos?cedula=1001`.
   - El endpoint es atendido en la clase de C# [PagosController.cs](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet/Controllers/PagosController.cs#L49) mediante `GetPaymentsByCedula`.
   - Este consulta la base de datos local `saludpay_db.pending_payments` filtrando por cédula del cliente y estado `"PENDIENTE_PAGO"` a través de `_pagoService.GetPaymentsByCedulaAsync(cedula)` ([PagoService.cs](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet/Services/PagoService.cs#L91)).
   - Devuelve la lista y se muestran solo los pagos pendientes del usuario en la pantalla de la SPA.
3. **Ejecución del Pago**:
   - El cliente hace clic en "Pagar" para el ID de compra correspondiente.
   - La SPA realiza un POST a `/api/pagos/{id}/pagar` que mapea a [PagosController.cs](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet/Controllers/PagosController.cs#L59) en `ExecutePayment`.
   - Llama a `_pagoService.ExecutePaymentAsync(id)` ([PagoService.cs](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet/Services/PagoService.cs#L110)), el cual cambia el estado de la factura a `"PAGADO"` localmente en la base de datos de pagos.
4. **Callback Asíncrono al Microservicio de Compra (Webhook)**:
   - Después del cambio de estado, el servicio ejecuta `SendPaymentCallbackAsync(payment)` ([PagoService.cs](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/saludpay-dotnet/Services/PagoService.cs#L137)).
   - Envía una petición POST HTTP con el estado del pago al webhook del microservicio de compras en `/compras/webhook-pago`.
   - En Java, la petición es recibida por [PurchaseController.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/controller/PurchaseController.java#L38) en `handlePagoWebhook`.
   - Este delega a [PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L120) en `handleWebhookPago`, donde el microservicio busca la compra en la base de datos y cambia su estado a `PAGADO`.

---

### 🏥 Flujo 5: Procesamiento de Compra Completada y Separación de Agendas (RabbitMQ)
**Objetivo:** Al confirmarse el pago, se notifica por correo electrónico, se finaliza la compra y se envían los datos a través de colas separadas a los microservicios de SHC (Historia Clínica) y SAM (Agenda Médica) para su registro definitivo.

1. **Notificación de Confirmación y Disparo de Evento**:
   - En `handleWebhookPago` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L120)), una vez actualizado el estado de la compra a `PAGADO`, se realizan tres operaciones inmediatas:
     - Envía un email simulado al cliente: `notificationService.sendConfirmationNotification(...)` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/NotificationService.java#L22)).
     - Llama a `publishPurchaseCompleted(purchase)` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L265)) para publicar el evento en las colas.
2. **Enriquecimiento del Evento y Publicación Asíncrona (RabbitMQ)**:
   - Para no duplicar datos, `publishPurchaseCompleted` consulta síncronamente al microservicio de catálogos mediante WebClient para enriquecer el evento con los nombres de los planes comprados y los nombres de los servicios médicos contratados.
   - Crea un objeto serializable `PurchaseCompletedEvent` ([PurchaseService.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/service/PurchaseService.java#L293)).
   - Publica el evento en el exchange `sps.exchange` de **RabbitMQ** direccionándolo a dos colas separadas usando sus respectivas Routing Keys:
     - **SHC (Historia Clínica)**: Routing Key: `purchase.completed.shc` a la cola `shc.queue` ([RabbitMqConfig.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/config/RabbitMqConfig.java#L19)).
     - **SAM (Agenda Médica)**: Routing Key: `purchase.completed.sam` a la cola `sam.queue` ([RabbitMqConfig.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/purchase-service/src/main/java/com/sps/purchase/config/RabbitMqConfig.java#L20)).
3. **Consumo y Guardado en Base de Datos de SHC**:
   - En el microservicio `shc-service`, el listener asíncrono [PurchaseCompletedListener.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/shc-service/src/main/java/com/sps/shc/listener/PurchaseCompletedListener.java#L27) con `@RabbitListener(queues = RabbitMqConfig.SHC_QUEUE)` captura el mensaje.
   - Procesa los datos de planes y del cliente, crea un registro de historia clínica `ShcRecord` y lo guarda en la base de datos `shc_db` mediante `repository.save(record)` ([PurchaseCompletedListener.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/shc-service/src/main/java/com/sps/shc/listener/PurchaseCompletedListener.java#L53)).
4. **Consumo y Guardado en Base de Datos de SAM**:
   - En el microservicio `sam-service`, el listener [PurchaseCompletedListener.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/sam-service/src/main/java/com/sps/sam/listener/PurchaseCompletedListener.java#L27) con `@RabbitListener(queues = RabbitMqConfig.SAM_QUEUE)` captura el mensaje.
   - Extrae los servicios médicos contratados para separar la agenda de los doctores especialistas.
   - Crea un registro de agenda médica `SamRecord` y lo persiste en la base de datos `sam_db` mediante `repository.save(record)` ([PurchaseCompletedListener.java](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/sam-service/src/main/java/com/sps/sam/listener/PurchaseCompletedListener.java#L52)).

---

## 🐋 Guía de DevOps e Infraestructura Docker

### 🛠️ Comandos Esenciales para Ejecutar la Aplicación

Para desplegar o depurar toda la arquitectura de microservicios, ejecuta los siguientes comandos desde la carpeta raíz del proyecto (donde está el archivo `docker-compose.yml`):

1. **Construir imágenes y arrancar contenedores en segundo plano (Recomendado)**:
   ```powershell
   docker-compose up -d --build
   ```
   *Nota: La bandera `--build` fuerza la reconstrucción de las imágenes Docker locales (Java, C#, Angular) asegurando que cualquier cambio de código se aplique.*

2. **Detener y eliminar todos los contenedores y redes asociadas**:
   ```powershell
   docker-compose down
   ```

3. **Ver el estado de los contenedores**:
   ```powershell
   docker-compose ps
   ```

4. **Ver los logs en vivo de un microservicio específico**:
   ```powershell
   docker-compose logs -f purchase-service
   ```

5. **Ver logs de todos los servicios al mismo tiempo**:
   ```powershell
   docker-compose logs -f
   ```

---

### 📦 Versiones Tecnológicas Definidas en los Dockerfiles

El proyecto cuenta con un stack moderno alineado en todas sus imágenes:

| Microservicio / Componente | Tecnologías Clave | Versión Base de Imagen (Dockerfile) | Puerto Expuesto / Interno |
| :--- | :--- | :--- | :--- |
| **MySQL Database** | MySQL Server 8.0 | `mysql:8.0` | `3306` (Externo e Interno) |
| **RabbitMQ Broker** | RabbitMQ + Management UI | `rabbitmq:3-management` | `5672` (Broker) / `15672` (Web UI) |
| **angular-frontend** | Angular SPA, Nginx Web Server | Node: `node:20-alpine` \| Nginx: `nginx:stable-alpine` | `4200` (Externo) -> `80` (Nginx Interno) |
| **api-gateway** | Spring Cloud Gateway (Java 17) | SDK: `maven:3.9.6-eclipse-temurin-17` \| JRE: `eclipse-temurin:17-jre-alpine` | `8080` (Externo e Interno) |
| **auth-service** | Spring Boot, JWT (Java 17) | SDK: `maven:3.9.6-eclipse-temurin-17` \| JRE: `eclipse-temurin:17-jre-alpine` | `8081` (Interno) |
| **catalog-service** | Spring Boot, JPA (Java 17) | SDK: `maven:3.9.6-eclipse-temurin-17` \| JRE: `eclipse-temurin:17-jre-alpine` | `8082` (Interno) |
| **purchase-service** | Spring Boot, WebFlux (Java 17) | SDK: `maven:3.9.6-eclipse-temurin-17` \| JRE: `eclipse-temurin:17-jre-alpine` | `8083` (Interno - Escalable en 2 réplicas) |
| **sns-mock** | Spring Boot Mock Service (Java 17) | SDK: `maven:3.9.6-eclipse-temurin-17` \| JRE: `eclipse-temurin:17-jre-alpine` | `8085` (Interno) |
| **saludpay (dotnet)** | ASP.NET Core API (C#) | SDK: `mcr.microsoft.com/dotnet/sdk:8.0` \| Runtime: `mcr.microsoft.com/dotnet/aspnet:8.0` | `8086` (Externo e Interno) |
| **shc-service** | Spring Boot, AMQP Rabbit (Java 17) | SDK: `maven:3.9.6-eclipse-temurin-17` \| JRE: `eclipse-temurin:17-jre-alpine` | `8087` (Interno) |
| **sam-service** | Spring Boot, AMQP Rabbit (Java 17) | SDK: `maven:3.9.6-eclipse-temurin-17` \| JRE: `eclipse-temurin:17-jre-alpine` | `8088` (Interno) |

---

### 🔀 Estructura de Redes y Aislamiento en Docker-Compose

El archivo [docker-compose.yml](file:///c:/Users/juand/OneDrive/Documentos/Arquisoft/SPS_arquisoftPro/docker-compose.yml) define dos redes (networks) para garantizar la seguridad y aislamiento de los datos:

1. **`sps-public-net`**: Red expuesta públicamente.
   - Contiene únicamente a `angular-frontend` y `api-gateway`.
   - La SPA en el navegador interactúa directamente con el puerto `8080` expuesto del Gateway.
2. **`sps-private-net`**: Red interna, privada y segura.
   - Contiene a todos los microservicios (`auth`, `catalog`, `purchase`, `shc`, `sam`, `saludpay`, `sns-mock`), la base de datos `mysql` y el broker `rabbitmq`.
   - **Ventaja de seguridad**: Ningún microservicio backend ni base de datos expone puertos directamente a la red exterior (a excepción del puerto mapeado temporalmente para pruebas). Todo el tráfico debe ser enrutado y validado a través de `api-gateway` para poder llegar a los servicios privados.

---

### 🔬 Monitoreo de Colas en RabbitMQ

RabbitMQ proporciona un portal de administración intuitivo para monitorear el flujo de mensajes entre el microservicio de compra y los sistemas SHC y SAM.

1. **Acceso a la interfaz**:
   - URL: `http://localhost:15672`
   - Credenciales (definidas en el archivo `.env`):
     - **Usuario**: `guest`
     - **Contraseña**: `guest`
2. **Qué inspeccionar durante la sustentación**:
   - **Exchanges**: En la pestaña *Exchanges*, busca `sps.exchange`. Este es el enrutador que recibe el evento enriquecido de compra completada.
   - **Queues (Colas)**: En la pestaña *Queues*, verás dos colas registradas:
     - `shc.queue`: Almacena temporalmente los mensajes destinados a la historia clínica.
     - `sam.queue`: Almacena temporalmente los mensajes destinados a la agenda médica.
   - **Verificar la llegada de mensajes**:
     - Al realizar un pago exitoso en el simulador, se puede observar cómo los contadores de mensajes (*Ready* y *Total*) aumentan en `1` momentáneamente y luego vuelven a `0` inmediatamente cuando los microservicios `shc-service` y `sam-service` consumen el evento asíncronamente y lo guardan en sus respectivas bases de datos.
     - Si apagas el contenedor de `shc-service` (`docker-compose stop shc-service`) y realizas un pago, el mensaje se quedará en cola en estado `Ready` dentro de `shc.queue`. Al volver a iniciar el contenedor, el listener reanudará su trabajo, consumirá el mensaje y lo borrará de la cola. Esto demuestra la **tolerancia a fallos y asincronía** del sistema.

---

### 🩺 Verificación E2E de Datos Finales vía API Gateway

Para demostrar al profesor que los datos se propagaron, integraron y enriquecieron correctamente en los microservicios finales, se pueden realizar peticiones HTTP de consulta a través del puerto público del **API Gateway** (`8080`):

1. **Consultar Registro en Historia Clínica (SHC)**:
   - **URL**: `GET http://localhost:8080/shc/historias/compra/{compraId}`
   - **Propósito**: Comprobar que la historia clínica registró la compra y contiene la lista enriquecida de planes y servicios médicos adquiridos en estado `PAGADO`.
   - **Ejemplo de respuesta**:
     ```json
     [
       {
         "id": 1,
         "compraId": 2,
         "clienteId": 2,
         "planIds": "1,2",
         "nombresPlanes": "Plan Basico, Plan Avanzado",
         "serviciosMedicos": "Consulta General, Examenes de Laboratorio, Hospitalizacion Basica, Consulta con Especialista, Examenes Avanzados, Hospitalizacion Especializada",
         "total": 409800.00,
         "estado": "PAGADO",
         "createdAt": "2026-05-26T04:28:50.162061Z"
       }
     ]
     ```

2. **Consultar Registro en Agenda Médica (SAM)**:
   - **URL**: `GET http://localhost:8080/sam/agendas/compra/{compraId}`
   - **Propósito**: Comprobar que se separó la agenda de citas médicas con los especialistas correspondientes para todos los servicios de salud contratados.
   - **Ejemplo de respuesta**:
     ```json
     [
       {
         "id": 1,
         "compraId": 2,
         "clienteId": 2,
         "planIds": "1,2",
         "nombresPlanes": "Plan Basico, Plan Avanzado",
         "serviciosMedicos": "Consulta General, Examenes de Laboratorio, Hospitalizacion Basica, Consulta con Especialista, Examenes Avanzados, Hospitalizacion Especializada",
         "total": 409800.00,
         "estado": "PAGADO",
         "createdAt": "2026-05-26T04:28:50.335835Z"
       }
     ]
     ```
