-- =========================================================
--  CHEFPRO - Esquema básico + datos de ejemplo
--  Tablas: users, menu, reservations
-- =========================================================

-- 1) Crear base de datos (si no existe) y usarla
DROP DATABASE IF EXISTS chefpro;
CREATE DATABASE chefpro
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE chefpro;

-- =========================================================
-- 2) Tabla USERS
--    - Usuarios del sistema: ADMIN, CHEF, COMENSAL
-- =========================================================

CREATE TABLE IF NOT EXISTS users (
    id            VARCHAR(50)                      NOT NULL,
    email         VARCHAR(150)                     NOT NULL,
    password      VARCHAR(255)                     NOT NULL,
    role          ENUM('ADMIN','CHEF','COMENSAL')  NOT NULL,
    name          VARCHAR(100)                     NOT NULL,
    phone_number  VARCHAR(20)                      NULL,
    created_at    TIMESTAMP                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- =========================================================
-- 3) Tabla MENU
--    - Menús creados por los CHEF
--    - dishes y allergens se guardan como texto separado por comas
-- =========================================================

CREATE TABLE IF NOT EXISTS menu (
    id                      VARCHAR(50)  NOT NULL,
    title                   VARCHAR(150) NOT NULL,
    description             VARCHAR(300) NULL,
    dishes                  TEXT         NOT NULL, -- Ej: 'Sopa,Entrecot,Flan'
    allergens               TEXT         NOT NULL, -- Ej: 'Gluten,Lácteos,Huevo'
    price_per_person        DECIMAL(10,2) NOT NULL,

    is_delivery_available   BOOLEAN      NOT NULL DEFAULT FALSE,
    can_cook_at_client_home BOOLEAN      NOT NULL DEFAULT FALSE,
    is_pickup_available     BOOLEAN      NOT NULL DEFAULT FALSE,

    chef_id                 VARCHAR(50)  NOT NULL, -- FK a users(id)
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_menu PRIMARY KEY (id),
    CONSTRAINT fk_menu_chef FOREIGN KEY (chef_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =========================================================
-- 4) Tabla RESERVATIONS
--    - Reservas realizadas por COMENSALES sobre MENÚS
--    - Incluye dirección de entrega y notas
-- =========================================================

CREATE TABLE IF NOT EXISTS reservations (
    id                VARCHAR(50)  NOT NULL,
    user_id           VARCHAR(50)  NOT NULL, -- COMENSAL
    menu_id           VARCHAR(50)  NOT NULL, -- MENÚ reservado

    reservation_date  DATETIME     NOT NULL, -- Fecha/hora de la reserva (servicio)
    number_of_people  INT          NOT NULL,

    -- Tipo de servicio según la lógica de tu menú
    service_type      ENUM('DELIVERY','HOME_COOKING','PICKUP') NOT NULL,

    -- Dirección de entrega (se usa sobre todo si service_type = DELIVERY)
    delivery_address      VARCHAR(200) NULL,
    delivery_city         VARCHAR(100) NULL,
    delivery_postal_code  VARCHAR(10)  NULL,
    delivery_notes        VARCHAR(300) NULL,

    status            ENUM('PENDING','CONFIRMED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_reservations PRIMARY KEY (id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_reservation_menu FOREIGN KEY (menu_id)
        REFERENCES menu(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- =========================================================
-- 5) DATOS DE EJEMPLO (SEED)
--    OJO: las contraseñas están en texto plano SOLO como ejemplo.
--    En vuestro entorno real debéis sustituirlas por hashes BCrypt.
-- =========================================================

-- 5.1 Usuarios de ejemplo
INSERT INTO users (id, email, password, role, name, phone_number)
VALUES
    ('u_001', 'admin@chefpro.com',    'admin1234',    'ADMIN',   'Admin ChefPro',   '600000001'),
    ('u_002', 'chef1@chefpro.com',    'chef1234',     'CHEF',    'Chef Mario',      '600000002'),
    ('u_003', 'cliente1@chefpro.com', 'comensal1234', 'COMENSAL','Comensal Laura',  '600000003');

-- =========================================================
--  IMPORTANTE:
--  Una vez tengáis vuestro PasswordGenerator con BCrypt,
--  debéis actualizar estos passwords así (ejemplo):
--
--  UPDATE users SET password = '<HASH_BCRYPT_ADMIN>'
--  WHERE email = 'admin@chefpro.com';
--
--  UPDATE users SET password = '<HASH_BCRYPT_CHEF>'
--  WHERE email = 'chef1@chefpro.com';
--
--  UPDATE users SET password = '<HASH_BCRYPT_COMENSAL>'
--  WHERE email = 'cliente1@chefpro.com';
-- =========================================================


-- 5.2 Menús de ejemplo
INSERT INTO menu (
    id, title, description, dishes, allergens, price_per_person,
    is_delivery_available, can_cook_at_client_home, is_pickup_available,
    chef_id
) VALUES
      (
          'm_001',
          'Menú Mediterráneo',
          'Ensalada, paella y postre casero.',
          'Ensalada,Paella,Flan de huevo',
          'Gluten,Lácteos,Huevo,Marisco',
          35.50,
          TRUE,
          FALSE,
          TRUE,
          'u_002' -- Chef Mario
      ),
      (
          'm_002',
          'Menú Vegano Ligero',
          'Crema de verduras, hummus y fruta fresca.',
          'Crema de verduras,Hummus,Fruta de temporada',
          'Frutos secos,Legumbres',
          28.00,
          TRUE,
          TRUE,
          TRUE,
          'u_002'
      );

-- 5.3 Reservas de ejemplo
INSERT INTO reservations (
    id, user_id, menu_id,
    reservation_date, number_of_people,
    service_type,
    delivery_address, delivery_city, delivery_postal_code, delivery_notes,
    status
) VALUES
      (
          'r_001',
          'u_003',     -- Comensal Laura
          'm_001',     -- Menú Mediterráneo
          '2026-02-10 21:00:00',
          4,
          'DELIVERY',
          'Calle Falsa 123',
          'Sevilla',
          '41001',
          'Llamar antes de llegar, por favor.',
          'PENDING'
      ),
      (
          'r_002',
          'u_003',
          'm_002',
          '2026-02-12 14:30:00',
          2,
          'PICKUP',
          NULL,
          NULL,
          NULL,
          'Recoger en local a las 14:15.',
          'CONFIRMED'
      );
