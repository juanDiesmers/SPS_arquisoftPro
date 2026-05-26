CREATE DATABASE IF NOT EXISTS sps_auth_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS sps_catalog_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS sps_purchase_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS saludpay_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS shc_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS sam_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS sns_db
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON sps_auth_db.* TO 'sps_user'@'%';
GRANT ALL PRIVILEGES ON sps_catalog_db.* TO 'sps_user'@'%';
GRANT ALL PRIVILEGES ON sps_purchase_db.* TO 'sps_user'@'%';
GRANT ALL PRIVILEGES ON saludpay_db.* TO 'sps_user'@'%';
GRANT ALL PRIVILEGES ON shc_db.* TO 'sps_user'@'%';
GRANT ALL PRIVILEGES ON sam_db.* TO 'sps_user'@'%';
GRANT ALL PRIVILEGES ON sns_db.* TO 'sps_user'@'%';

FLUSH PRIVILEGES;

USE sps_auth_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    cedula VARCHAR(50),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

USE sps_catalog_db;

CREATE TABLE IF NOT EXISTS planes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(12,2) NOT NULL,
    convenio VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS servicios_medicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS plan_servicios (
    plan_id BIGINT NOT NULL,
    servicio_id BIGINT NOT NULL,
    PRIMARY KEY (plan_id, servicio_id),
    FOREIGN KEY (plan_id) REFERENCES planes(id) ON DELETE CASCADE,
    FOREIGN KEY (servicio_id) REFERENCES servicios_medicos(id) ON DELETE CASCADE
);

-- Seed initial medical services
INSERT IGNORE INTO servicios_medicos (id, nombre, precio) VALUES (1, 'Consulta General', 50000.00);
INSERT IGNORE INTO servicios_medicos (id, nombre, precio) VALUES (2, 'Examenes de Laboratorio', 30000.00);
INSERT IGNORE INTO servicios_medicos (id, nombre, precio) VALUES (3, 'Hospitalizacion Basica', 49900.00);
INSERT IGNORE INTO servicios_medicos (id, nombre, precio) VALUES (4, 'Consulta con Especialista', 100000.00);
INSERT IGNORE INTO servicios_medicos (id, nombre, precio) VALUES (5, 'Examenes Avanzados', 80000.00);
INSERT IGNORE INTO servicios_medicos (id, nombre, precio) VALUES (6, 'Hospitalizacion Especializada', 99900.00);
INSERT IGNORE INTO servicios_medicos (id, nombre, precio) VALUES (7, 'Cobertura Medica Familiar', 399900.00);

-- Seed initial plans
INSERT IGNORE INTO planes (id, nombre, descripcion, precio, convenio, activo) VALUES (1, 'Plan Basico', 'Cobertura de consulta general y laboratorio.', 129900.00, 'Convenio Nacional', 1);
INSERT IGNORE INTO planes (id, nombre, descripcion, precio, convenio, activo) VALUES (2, 'Plan Avanzado', 'Cobertura de especialistas y examenes avanzados.', 279900.00, 'Convenio Premium', 1);
INSERT IGNORE INTO planes (id, nombre, descripcion, precio, convenio, activo) VALUES (3, 'Plan Familiar', 'Cobertura para hasta 4 miembros de la familia.', 399900.00, 'Convenio Familiar', 1);

-- Link plans and services
INSERT IGNORE INTO plan_servicios (plan_id, servicio_id) VALUES (1, 1);
INSERT IGNORE INTO plan_servicios (plan_id, servicio_id) VALUES (1, 2);
INSERT IGNORE INTO plan_servicios (plan_id, servicio_id) VALUES (1, 3);
INSERT IGNORE INTO plan_servicios (plan_id, servicio_id) VALUES (2, 4);
INSERT IGNORE INTO plan_servicios (plan_id, servicio_id) VALUES (2, 5);
INSERT IGNORE INTO plan_servicios (plan_id, servicio_id) VALUES (2, 6);
INSERT IGNORE INTO plan_servicios (plan_id, servicio_id) VALUES (3, 7);

USE sps_purchase_db;

CREATE TABLE IF NOT EXISTS compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    cedula VARCHAR(50),
    estado VARCHAR(50) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    payload JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);

USE saludpay_db;

CREATE TABLE IF NOT EXISTS pending_payments (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    CompraId BIGINT NOT NULL,
    ClienteId BIGINT NOT NULL,
    Total DECIMAL(12,2) NOT NULL,
    Estado VARCHAR(50) NOT NULL,
    Cedula VARCHAR(50) NOT NULL DEFAULT '1001',
    CreatedAt DATETIME(6),
    UpdatedAt DATETIME(6)
);

CREATE TABLE IF NOT EXISTS saludpay_users (
    Id INT AUTO_INCREMENT PRIMARY KEY,
    Cedula VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL
);

INSERT IGNORE INTO saludpay_users (Cedula, Password) VALUES ('1001', 'password123');
INSERT IGNORE INTO saludpay_users (Cedula, Password) VALUES ('1002', 'password123');

USE shc_db;

CREATE TABLE IF NOT EXISTS historias_clinicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

USE sam_db;

CREATE TABLE IF NOT EXISTS agendas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    compra_id BIGINT NOT NULL,
    servicio VARCHAR(150) NOT NULL,
    doctor VARCHAR(150) NOT NULL,
    fecha_cita DATETIME,
    estado VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);