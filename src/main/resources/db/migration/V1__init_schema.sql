-- =====================================================================
-- ESQUEMA COMPLETO - POS MULTI-SUCURSAL CON SINCRONIZACIÓN OFFLINE
-- Motor: PostgreSQL 13+ (usa gen_random_uuid() nativo)
--
-- Convenciones:
--   * id   : clave interna (BIGSERIAL), nunca se expone por la API.
--   * uuid : identificador público. En tablas transaccionales lo genera
--            la caja offline, lo que da idempotencia al sincronizar.
--   * created_at : momento en que ocurrió el hecho (hora local de la caja).
--   * synced_at  : momento en que llegó al servidor.
--   * Precios sin impuesto; el impuesto se suma aparte.
-- =====================================================================


-- =====================================================================
-- 0. FUNCIÓN AUXILIAR: mantiene updated_at automáticamente
-- =====================================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- =====================================================================
-- 1. PERSONAS
-- =====================================================================

-- ---------------------------------------------------------------------
-- people
-- Datos personales comunes. Una persona puede ser a la vez usuario del
-- sistema, cliente y/o empleado (cada rol vive en su propia tabla).
-- ---------------------------------------------------------------------
CREATE TABLE people (
                        id              BIGSERIAL PRIMARY KEY,
                        uuid            UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                        first_name      VARCHAR(100) NOT NULL,
                        last_name       VARCHAR(100) NOT NULL,
                        document_type   VARCHAR(20)  NOT NULL,              -- CC, NIT, CE, PASAPORTE...
                        document_number VARCHAR(50)  NOT NULL,
                        phone           VARCHAR(50),
                        address         TEXT,
                        created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        synced_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- El documento es único por tipo + número
                        CONSTRAINT uq_people_document UNIQUE (document_type, document_number)
);
COMMENT ON TABLE people IS 'Datos personales comunes a usuarios, clientes y empleados';


-- =====================================================================
-- 2. CONTROL DE ACCESO (RBAC)
-- =====================================================================

-- ---------------------------------------------------------------------
-- permissions
-- Acciones atómicas del sistema (ej. SALE_CREATE, PRODUCT_UPDATE).
-- ---------------------------------------------------------------------
CREATE TABLE permissions (
                             id          BIGSERIAL PRIMARY KEY,
                             uuid        UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                             name        VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(200),
                             created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE permissions IS 'Permisos atómicos que se pueden otorgar a un rol';

-- ---------------------------------------------------------------------
-- roles
-- Agrupación de permisos (ej. ADMIN, CAJERO, BODEGUERO).
-- ---------------------------------------------------------------------
CREATE TABLE roles (
                       id          BIGSERIAL PRIMARY KEY,
                       uuid        UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                       name        VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(200),
                       is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE roles IS 'Roles del sistema; agrupan permisos';

-- ---------------------------------------------------------------------
-- role_permissions
-- Relación N:M entre roles y permisos.
-- ---------------------------------------------------------------------
CREATE TABLE role_permissions (
                                  role_id       BIGINT NOT NULL REFERENCES roles(id)       ON DELETE CASCADE,
                                  permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
                                  PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
COMMENT ON TABLE role_permissions IS 'Permisos asignados a cada rol (N:M)';


-- =====================================================================
-- 3. USUARIOS Y ACTORES
-- =====================================================================

-- ---------------------------------------------------------------------
-- users
-- Credenciales de acceso al sistema. Cada usuario es exactamente una
-- persona (relación 1:1 con people).
-- ---------------------------------------------------------------------
CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       uuid          UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                       person_id     BIGINT NOT NULL UNIQUE REFERENCES people(id) ON DELETE RESTRICT,
                       username      VARCHAR(100) NOT NULL,
                       email         VARCHAR(150) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,                -- bcrypt/argon2, nunca texto plano
                       is_active     BOOLEAN NOT NULL DEFAULT TRUE,
                       last_login_at TIMESTAMP WITH TIME ZONE,
                       created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- Username y email únicos sin distinguir mayúsculas/minúsculas
CREATE UNIQUE INDEX uq_users_username_lower ON users (LOWER(username));
CREATE UNIQUE INDEX uq_users_email_lower    ON users (LOWER(email));
COMMENT ON TABLE users IS 'Cuentas de acceso al sistema (1:1 con people)';

-- ---------------------------------------------------------------------
-- user_roles
-- Relación N:M entre usuarios y roles.
-- ---------------------------------------------------------------------
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
COMMENT ON TABLE user_roles IS 'Roles asignados a cada usuario (N:M)';

-- ---------------------------------------------------------------------
-- customers
-- Persona que compra. credit_limit es el cupo máximo de crédito;
-- el saldo se calcula en la vista customer_balances.
-- ---------------------------------------------------------------------
CREATE TABLE customers (
                           id           BIGSERIAL PRIMARY KEY,
                           uuid         UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                           person_id    BIGINT NOT NULL UNIQUE REFERENCES people(id) ON DELETE RESTRICT,
                           credit_limit NUMERIC(12, 2) DEFAULT 0 CHECK (credit_limit >= 0),
                           is_active    BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE customers IS 'Clientes y su cupo de crédito (1:1 con people)';

-- ---------------------------------------------------------------------
-- employees
-- Datos laborales de una persona. Un empleado no necesariamente
-- tiene usuario del sistema.
-- ---------------------------------------------------------------------
CREATE TABLE employees (
                           id         BIGSERIAL PRIMARY KEY,
                           uuid       UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                           person_id  BIGINT NOT NULL UNIQUE REFERENCES people(id) ON DELETE RESTRICT,
                           job_title  VARCHAR(100),
                           salary     NUMERIC(12, 2) CHECK (salary >= 0),
                           hire_date  DATE NOT NULL,
                           is_active  BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE employees IS 'Datos laborales de empleados (1:1 con people)';


-- =====================================================================
-- 4. UBICACIONES Y CATÁLOGO
-- =====================================================================

-- ---------------------------------------------------------------------
-- locations
-- Sucursales, bodegas o puntos de venta con inventario propio.
-- ---------------------------------------------------------------------
CREATE TABLE locations (
                           id         BIGSERIAL PRIMARY KEY,
                           uuid       UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                           name       VARCHAR(100) NOT NULL UNIQUE,
                           address    TEXT,
                           is_active  BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE locations IS 'Sucursales o bodegas con inventario propio';

-- ---------------------------------------------------------------------
-- terminals
-- Terminales físicas (cajas registradoras/dispositivos) de una sucursal.
-- Permite varias cajas por sucursal.
-- ---------------------------------------------------------------------
CREATE TABLE terminals (
                           id          BIGSERIAL PRIMARY KEY,
                           uuid        UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                           location_id BIGINT NOT NULL REFERENCES locations(id) ON DELETE RESTRICT,
                           name        VARCHAR(100) NOT NULL,
                           is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT uq_terminal_name_location UNIQUE (location_id, name)
);
COMMENT ON TABLE terminals IS 'Terminales/dispositivos de caja de cada sucursal';

-- ---------------------------------------------------------------------
-- categories
-- Clasificación de productos.
-- ---------------------------------------------------------------------
CREATE TABLE categories (
                            id          BIGSERIAL PRIMARY KEY,
                            uuid        UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                            name        VARCHAR(150) NOT NULL UNIQUE,
                            description TEXT,
                            created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                            updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE categories IS 'Categorías de productos';

-- ---------------------------------------------------------------------
-- products
-- Catálogo global de productos. El stock por sucursal está en
-- product_stocks; aquí solo precios y datos descriptivos.
-- ---------------------------------------------------------------------
CREATE TABLE products (
                          id          BIGSERIAL PRIMARY KEY,
                          uuid        UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                          category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
                          barcode     VARCHAR(50)  NOT NULL UNIQUE,
                          name        VARCHAR(150) NOT NULL,
                          description TEXT,
                          cost_price  NUMERIC(12, 2) NOT NULL CHECK (cost_price >= 0),
                          sale_price  NUMERIC(12, 2) NOT NULL CHECK (sale_price >= 0),
                          tax_rate    NUMERIC(5, 2)  NOT NULL DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),  -- % de impuesto
                          is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_products_category_id ON products(category_id);
COMMENT ON TABLE products IS 'Catálogo global de productos con precios e impuesto';


-- =====================================================================
-- 5. CAJAS Y VENTAS (transacciones híbridas online/offline)
-- =====================================================================

-- ---------------------------------------------------------------------
-- cash_registers
-- Turno de caja: apertura y cierre por un usuario en una terminal.
-- Solo puede haber una caja abierta por usuario y por terminal.
-- ---------------------------------------------------------------------
CREATE TABLE cash_registers (
                                id               BIGSERIAL PRIMARY KEY,
                                uuid             UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                                location_id      BIGINT NOT NULL REFERENCES locations(id) ON DELETE RESTRICT,
                                terminal_id      BIGINT REFERENCES terminals(id) ON DELETE RESTRICT,
                                user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                                opening_balance  NUMERIC(12, 2) NOT NULL CHECK (opening_balance >= 0),
                                closing_balance  NUMERIC(12, 2) CHECK (closing_balance >= 0),   -- contado al cerrar
                                expected_balance NUMERIC(12, 2),                                -- calculado por el sistema
                                difference       NUMERIC(12, 2),                                -- closing - expected
                                notes            TEXT,
                                opened_at        TIMESTAMP WITH TIME ZONE NOT NULL,
                                closed_at        TIMESTAMP WITH TIME ZONE,
                                is_open          BOOLEAN NOT NULL DEFAULT TRUE,
                                synced_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    -- Coherencia entre estado y datos de cierre
                                CONSTRAINT chk_cash_register_state CHECK (
                                    (is_open AND closed_at IS NULL AND closing_balance IS NULL)
                                        OR
                                    (NOT is_open AND closed_at IS NOT NULL AND closing_balance IS NOT NULL)
                                    ),
                                CONSTRAINT chk_cash_register_dates CHECK (closed_at IS NULL OR closed_at >= opened_at)
);
CREATE UNIQUE INDEX uq_open_register_per_user     ON cash_registers(user_id) WHERE is_open;
CREATE UNIQUE INDEX uq_open_register_per_terminal ON cash_registers(terminal_id)
    WHERE is_open AND terminal_id IS NOT NULL;
CREATE INDEX idx_cash_registers_location_opened ON cash_registers(location_id, opened_at);
CREATE INDEX idx_cash_registers_user_id         ON cash_registers(user_id);
COMMENT ON TABLE cash_registers IS 'Turnos de caja (apertura/cierre) con cuadre de efectivo';

-- ---------------------------------------------------------------------
-- sales
-- Cabecera de la venta. El uuid lo genera la caja (idempotencia).
-- customer_id es opcional (consumidor final), salvo en ventas a crédito.
-- total = subtotal - discount_amount + tax_amount
-- ---------------------------------------------------------------------
CREATE TABLE sales (
                       id               BIGSERIAL PRIMARY KEY,
                       uuid             UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),  -- recibido desde la caja offline
                       cash_register_id BIGINT NOT NULL REFERENCES cash_registers(id) ON DELETE RESTRICT,
                       location_id      BIGINT NOT NULL REFERENCES locations(id)      ON DELETE RESTRICT,
                       user_id          BIGINT NOT NULL REFERENCES users(id)          ON DELETE RESTRICT,
                       customer_id      BIGINT REFERENCES customers(id)               ON DELETE RESTRICT,
                       receipt_number   VARCHAR(50),                                   -- número de comprobante
                       status           VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
                       payment_method   VARCHAR(30) NOT NULL,
                       subtotal         NUMERIC(12, 2) NOT NULL DEFAULT 0,             -- suma de líneas, sin impuesto
                       discount_amount  NUMERIC(12, 2) NOT NULL DEFAULT 0,             -- descuento global
                       tax_amount       NUMERIC(12, 2) NOT NULL DEFAULT 0,
                       total_amount     NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
                       notes            TEXT,
    -- Anulación
                       voided_at        TIMESTAMP WITH TIME ZONE,
                       voided_by        BIGINT REFERENCES users(id) ON DELETE RESTRICT,
                       void_reason      TEXT,
                       created_at       TIMESTAMP WITH TIME ZONE NOT NULL,             -- hora local de la caja
                       synced_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- hora de llegada al servidor

                       CONSTRAINT chk_sales_status CHECK (status IN ('COMPLETED', 'VOIDED')),
                       CONSTRAINT chk_sales_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'TRANSFER', 'CREDIT')),
                       CONSTRAINT chk_sales_amounts CHECK (
                           subtotal >= 0 AND discount_amount >= 0 AND tax_amount >= 0
                               AND total_amount = subtotal - discount_amount + tax_amount
                           ),
                       CONSTRAINT chk_sales_void CHECK (
                           (status = 'VOIDED' AND voided_at IS NOT NULL AND voided_by IS NOT NULL)
                               OR (status = 'COMPLETED' AND voided_at IS NULL)
                           ),
    -- Una venta a crédito requiere cliente identificado
                       CONSTRAINT chk_sales_credit_customer CHECK (payment_method <> 'CREDIT' OR customer_id IS NOT NULL),
    -- Comprobante único por caja (permite numeración offline)
                       CONSTRAINT uq_sales_receipt UNIQUE (cash_register_id, receipt_number)
);
CREATE INDEX idx_sales_cash_register_id ON sales(cash_register_id);
CREATE INDEX idx_sales_customer_id      ON sales(customer_id) WHERE customer_id IS NOT NULL;
CREATE INDEX idx_sales_location_created ON sales(location_id, created_at);
CREATE INDEX idx_sales_user_created     ON sales(user_id, created_at);
CREATE INDEX idx_sales_created_at       ON sales(created_at);
CREATE INDEX idx_sales_synced_at        ON sales(synced_at);
COMMENT ON TABLE sales IS 'Cabecera de ventas; puede originarse en cajas offline';

-- ---------------------------------------------------------------------
-- sale_details
-- Líneas de la venta. Guarda precio y costo históricos para que los
-- reportes y márgenes no cambien si el producto se modifica después.
-- subtotal de línea = quantity * unit_price - discount_amount (sin impuesto)
-- ---------------------------------------------------------------------
CREATE TABLE sale_details (
                              id              BIGSERIAL PRIMARY KEY,
                              uuid            UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                              sale_id         BIGINT NOT NULL REFERENCES sales(id)    ON DELETE CASCADE,
                              product_id      BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                              quantity        INT NOT NULL CHECK (quantity > 0),
                              unit_price      NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
                              unit_cost       NUMERIC(12, 2) NOT NULL CHECK (unit_cost >= 0),   -- costo al momento de la venta
                              discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
                              tax_rate        NUMERIC(5, 2)  NOT NULL DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),
                              tax_amount      NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
                              subtotal        NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
                              CONSTRAINT chk_sale_details_subtotal CHECK (subtotal = quantity * unit_price - discount_amount)
);
CREATE INDEX idx_sale_details_sale_id    ON sale_details(sale_id);
CREATE INDEX idx_sale_details_product_id ON sale_details(product_id);
COMMENT ON TABLE sale_details IS 'Líneas de cada venta con precio y costo históricos';

-- ---------------------------------------------------------------------
-- customer_payments
-- Abonos de clientes a su deuda (cuentas por cobrar).
-- Saldo = ventas a crédito vigentes - abonos (ver vista customer_balances).
-- ---------------------------------------------------------------------
CREATE TABLE customer_payments (
                                   id               BIGSERIAL PRIMARY KEY,
                                   uuid             UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                                   customer_id      BIGINT NOT NULL REFERENCES customers(id)      ON DELETE RESTRICT,
                                   cash_register_id BIGINT REFERENCES cash_registers(id)          ON DELETE RESTRICT,
                                   user_id          BIGINT NOT NULL REFERENCES users(id)          ON DELETE RESTRICT,
                                   amount           NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
                                   payment_method   VARCHAR(30) NOT NULL CHECK (payment_method IN ('CASH', 'CARD', 'TRANSFER')),
                                   notes            TEXT,
                                   created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
                                   synced_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_customer_payments_customer ON customer_payments(customer_id, created_at);
CREATE INDEX idx_customer_payments_cash_register ON customer_payments(cash_register_id)
    WHERE cash_register_id IS NOT NULL;
COMMENT ON TABLE customer_payments IS 'Abonos de clientes a sus ventas a crédito';


-- =====================================================================
-- 6. INVENTARIO
-- =====================================================================

-- ---------------------------------------------------------------------
-- product_stocks
-- Existencias actuales de cada producto en cada sucursal.
-- Se permite quantity negativa: una venta offline ya ocurrió físicamente
-- y se compensa con alertas (idx_product_stocks_low). Para rechazar
-- stock negativo, añadir CHECK (quantity >= 0).
-- ---------------------------------------------------------------------
CREATE TABLE product_stocks (
                                id          BIGSERIAL PRIMARY KEY,
                                uuid        UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                                location_id BIGINT NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
                                product_id  BIGINT NOT NULL REFERENCES products(id)  ON DELETE CASCADE,
                                quantity    INT NOT NULL DEFAULT 0,
                                min_stock   INT NOT NULL DEFAULT 5 CHECK (min_stock >= 0),     -- umbral de alerta
                                updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT uq_product_location UNIQUE (product_id, location_id)
);
CREATE INDEX idx_product_stocks_location_id ON product_stocks(location_id);
-- Alertas de stock bajo o negativo
CREATE INDEX idx_product_stocks_low ON product_stocks(location_id) WHERE quantity <= min_stock;
COMMENT ON TABLE product_stocks IS 'Existencias actuales por producto y sucursal';

-- ---------------------------------------------------------------------
-- stock_movements
-- Libro de movimientos de inventario (auditoría). Todo cambio en
-- product_stocks debe tener su movimiento. Las transferencias generan
-- dos movimientos (TRANSFER_OUT y TRANSFER_IN) unidos por transfer_uuid.
-- ---------------------------------------------------------------------
CREATE TABLE stock_movements (
                                 id            BIGSERIAL PRIMARY KEY,
                                 uuid          UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                                 user_id       BIGINT NOT NULL REFERENCES users(id)     ON DELETE RESTRICT,
                                 product_id    BIGINT NOT NULL REFERENCES products(id)  ON DELETE RESTRICT,
                                 location_id   BIGINT NOT NULL REFERENCES locations(id) ON DELETE RESTRICT,
                                 sale_id       BIGINT REFERENCES sales(id)              ON DELETE RESTRICT,  -- si proviene de una venta
                                 transfer_uuid UUID,                                                         -- agrupa el par de una transferencia
                                 movement_type VARCHAR(30) NOT NULL,
                                 quantity      INT NOT NULL CHECK (quantity > 0),        -- siempre positiva; el tipo define el signo
                                 reason        TEXT,
                                 created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
                                 synced_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT chk_stock_movement_type CHECK (movement_type IN (
                                                                                             'PURCHASE', 'SALE', 'SALE_VOID', 'RETURN',
                                                                                             'ADJUSTMENT_IN', 'ADJUSTMENT_OUT', 'TRANSFER_IN', 'TRANSFER_OUT'
                                     ))
);
CREATE INDEX idx_stock_movements_product_created  ON stock_movements(product_id, created_at);
CREATE INDEX idx_stock_movements_location_created ON stock_movements(location_id, created_at);
CREATE INDEX idx_stock_movements_user_id          ON stock_movements(user_id);
CREATE INDEX idx_stock_movements_sale_id          ON stock_movements(sale_id) WHERE sale_id IS NOT NULL;
CREATE INDEX idx_stock_movements_transfer         ON stock_movements(transfer_uuid) WHERE transfer_uuid IS NOT NULL;
COMMENT ON TABLE stock_movements IS 'Historial auditable de entradas, salidas, ajustes y transferencias';


-- =====================================================================
-- 7. VISTAS
-- =====================================================================

-- ---------------------------------------------------------------------
-- customer_balances
-- Saldo adeudado y crédito disponible por cliente.
-- Saldo = ventas a crédito no anuladas - abonos.
-- ---------------------------------------------------------------------
CREATE VIEW customer_balances AS
SELECT
    c.id   AS customer_id,
    c.uuid AS customer_uuid,
    c.credit_limit,
    COALESCE(s.credit_total, 0) - COALESCE(p.paid_total, 0) AS balance,
    c.credit_limit - (COALESCE(s.credit_total, 0) - COALESCE(p.paid_total, 0)) AS available_credit
FROM customers c
         LEFT JOIN (
    SELECT customer_id, SUM(total_amount) AS credit_total
    FROM sales
    WHERE payment_method = 'CREDIT' AND status = 'COMPLETED'
    GROUP BY customer_id
) s ON s.customer_id = c.id
         LEFT JOIN (
    SELECT customer_id, SUM(amount) AS paid_total
    FROM customer_payments
    GROUP BY customer_id
) p ON p.customer_id = c.id;
COMMENT ON VIEW customer_balances IS 'Saldo y crédito disponible por cliente';


-- =====================================================================
-- 8. TRIGGERS DE updated_at
-- =====================================================================
DO $$
DECLARE
t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'people', 'users', 'roles', 'customers', 'employees',
        'locations', 'terminals', 'categories', 'products', 'product_stocks'
    ]
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%1$s_updated_at BEFORE UPDATE ON %1$s
             FOR EACH ROW EXECUTE FUNCTION set_updated_at()', t);
END LOOP;
END $$;