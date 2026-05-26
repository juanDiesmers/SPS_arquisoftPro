import os

base_dir = r"c:\Users\juand\SPS_arquisoftPro\docs\diagramas"

files = {
    "componentes.md": """# Diagrama de Componentes

```mermaid
graph TD
    UI[Angular SPA] --> AG[API Gateway]
    AG --> Auth[Auth Service]
    AG --> Cat[Catalog Service]
    AG --> Pur[Purchase Service]
    Pur --> SNS[SNS Mock]
    Pur --> SP[Salud Pay .NET]
    Pur -.->|Publish| RMQ[RabbitMQ]
    RMQ -.->|Consume| SHC[SHC Service]
    RMQ -.->|Consume| SAM[SAM Service]
    
    Auth --> DB_Auth[(sps_auth_db)]
    Cat --> DB_Cat[(sps_catalog_db)]
    Pur --> DB_Pur[(sps_purchase_db)]
    SP --> DB_SP[(saludpay_db)]
    SNS --> DB_SNS[(sns_db)]
    SHC --> DB_SHC[(shc_db)]
    SAM --> DB_SAM[(sam_db)]
    
    classDef frontend fill:#e1f5fe,stroke:#01579b;
    classDef gateway fill:#fff3e0,stroke:#e65100;
    classDef core fill:#e8f5e9,stroke:#1b5e20;
    classDef external fill:#fce4ec,stroke:#880e4f;
    classDef db fill:#f3e5f5,stroke:#4a148c;
    classDef queue fill:#ffebee,stroke:#b71c1c;

    class UI frontend;
    class AG gateway;
    class Auth,Cat,Pur,SHC,SAM core;
    class SNS,SP external;
    class DB_Auth,DB_Cat,DB_Pur,DB_SP,DB_SNS,DB_SHC,DB_SAM db;
    class RMQ queue;
```
""",

    "despliegue.md": """# Diagrama de Despliegue

```mermaid
graph TD
    subgraph "Docker Host"
        Nginx[Nginx Container]
        Gateway[API Gateway Container]
        MySQL[(MySQL Container)]
        RMQ[[RabbitMQ Container]]
        
        subgraph "Spring Boot Services"
            Auth[Auth Service]
            Catalog[Catalog Service]
            Purchase1[Purchase Service - Replica 1]
            Purchase2[Purchase Service - Replica 2]
            SHC[SHC Service]
            SAM[SAM Service]
            SNS[SNS Mock]
        end
        
        subgraph ".NET 8 Services"
            SaludPay[Salud Pay Service]
        end
        
        Nginx --> Gateway
        Gateway --> Auth
        Gateway --> Catalog
        Gateway --> Purchase1
        Gateway --> Purchase2
        
        Purchase1 -.-> RMQ
        Purchase2 -.-> RMQ
        Purchase1 --> SNS
        Purchase2 --> SNS
        Purchase1 --> SaludPay
        Purchase2 --> SaludPay
        
        RMQ -.-> SHC
        RMQ -.-> SAM
        
        Auth --> MySQL
        Catalog --> MySQL
        Purchase1 --> MySQL
        Purchase2 --> MySQL
        SHC --> MySQL
        SAM --> MySQL
        SNS --> MySQL
        SaludPay --> MySQL
    end
```
""",

    "flujo_compra.md": """# Flujo de Compra

```mermaid
sequenceDiagram
    actor Usuario
    participant UI as Angular SPA
    participant GW as API Gateway
    participant Cat as Catalog Service
    participant Pur as Purchase Service
    participant SNS as SNS Mock
    participant SP as Salud Pay

    Usuario->>UI: Ve catalogo de planes
    UI->>GW: GET /api/catalog/planes
    GW->>Cat: GET /planes
    Cat-->>GW: Lista de planes
    GW-->>UI: Lista de planes
    
    Usuario->>UI: Agrega planes al carrito
    Usuario->>UI: Clic en Confirmar Compra
    UI->>GW: POST /api/purchase/compras
    GW->>Pur: POST /compras
    
    Pur->>Pur: Guarda estado VALIDANDO_SNS
    Pur-->>GW: Compra ID + Estado
    GW-->>UI: Compra Creada
    
    Pur-)SNS: POST /sns/validar (Asincrono)
    SNS-->>Pur: Webhook APROBADO (o RECHAZADO)
    Pur->>Pur: Actualiza a PENDIENTE_PAGO
    Pur->>SP: POST /api/pagos/pendientes
    
    Usuario->>UI: Revisa Estado
    UI->>GW: GET /api/purchase/compras/{id}
    GW->>Pur: GET /compras/{id}
    Pur-->>GW: Estado PENDIENTE_PAGO
    GW-->>UI: Muestra PENDIENTE_PAGO
    
    Usuario->>UI: Clic en Ir a Salud Pay
    UI->>GW: POST /api/saludpay/pagar
    GW->>SP: POST /api/pagos/pagar
    SP-->>GW: Exito (redirige)
    GW-->>UI: Muestra Pago Exitoso
    
    SP-)Pur: POST /compras/webhook-pago (Webhook)
    Pur->>Pur: Actualiza a PAGADO
    Pur->>Pur: Publica en RabbitMQ
```
""",

    "flujo_rabbitmq.md": """# Flujo RabbitMQ

```mermaid
sequenceDiagram
    participant Pur as Purchase Service
    participant Ex as sps.exchange
    participant Q_SHC as shc.queue
    participant Q_SAM as sam.queue
    participant SHC as SHC Service
    participant SAM as SAM Service

    Pur->>Ex: publish(PurchaseCompletedEvent)
    Ex-->>Q_SHC: routing: purchase.completed.shc
    Ex-->>Q_SAM: routing: purchase.completed.sam
    
    Q_SHC->>SHC: consume()
    SHC->>SHC: Crea Historia Clinica
    
    Q_SAM->>SAM: consume()
    SAM->>SAM: Agendar cita / servicio
```
"""
}

os.makedirs(base_dir, exist_ok=True)
for rel_path, content in files.items():
    full_path = os.path.join(base_dir, rel_path)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content)

print("Documentation diagrams generated successfully.")
