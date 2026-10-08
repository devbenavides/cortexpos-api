
-- =====================================================================
-- V2: Datos iniciales de seguridad
--   * Permisos del sistema
--   * Roles: ADMIN, CASHIER, WAREHOUSE
--   * Usuario administrador inicial
-- =====================================================================

-- ---------- Permisos ----------
INSERT INTO permissions (name, description) VALUES
                                                ('USER_READ',           'Consultar usuarios'),
                                                ('USER_CREATE',         'Crear usuarios'),
                                                ('USER_UPDATE',         'Editar y activar/desactivar usuarios'),
                                                ('ROLE_READ',           'Consultar roles y permisos'),
                                                ('ROLE_MANAGE',         'Crear y editar roles'),
                                                ('LOCATION_READ',       'Consultar sucursales'),
                                                ('LOCATION_CREATE',     'Crear sucursales'),
                                                ('LOCATION_UPDATE',     'Editar y activar/desactivar sucursales'),
                                                ('PRODUCT_READ',        'Consultar productos y categorías'),
                                                ('PRODUCT_CREATE',      'Crear productos y categorías'),
                                                ('PRODUCT_UPDATE',      'Editar productos y categorías'),
                                                ('CUSTOMER_READ',       'Consultar clientes'),
                                                ('CUSTOMER_CREATE',     'Crear clientes'),
                                                ('CUSTOMER_UPDATE',     'Editar clientes'),
                                                ('EMPLOYEE_READ',       'Consultar empleados'),
                                                ('EMPLOYEE_CREATE',     'Crear empleados'),
                                                ('EMPLOYEE_UPDATE',     'Editar empleados'),
                                                ('INVENTORY_READ',      'Consultar existencias y movimientos'),
                                                ('INVENTORY_ADJUST',    'Registrar entradas y ajustes de inventario'),
                                                ('INVENTORY_TRANSFER',  'Transferir inventario entre sucursales'),
                                                ('CASH_REGISTER_READ',  'Consultar cajas'),
                                                ('CASH_REGISTER_OPEN',  'Abrir caja'),
                                                ('CASH_REGISTER_CLOSE', 'Cerrar caja'),
                                                ('SALE_CREATE',         'Registrar ventas'),
                                                ('SALE_READ',           'Consultar ventas'),
                                                ('SALE_VOID',           'Anular ventas'),
                                                ('REPORT_READ',         'Consultar reportes'),
                                                ('SYNC_EXECUTE',        'Ejecutar sincronización offline')
    ON CONFLICT (name) DO NOTHING;

-- ---------- Roles ----------
INSERT INTO roles (name, description) VALUES
                                          ('ADMIN',     'Administrador con todos los permisos'),
                                          ('CASHIER',   'Cajero: ventas y manejo de su caja'),
                                          ('WAREHOUSE', 'Bodeguero: gestión de inventario')
    ON CONFLICT (name) DO NOTHING;

-- ADMIN: todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
    ON CONFLICT DO NOTHING;

-- CASHIER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.name IN (
                                              'LOCATION_READ', 'PRODUCT_READ', 'CUSTOMER_READ', 'CUSTOMER_CREATE',
                                              'CASH_REGISTER_READ', 'CASH_REGISTER_OPEN', 'CASH_REGISTER_CLOSE',
                                              'SALE_CREATE', 'SALE_READ', 'SYNC_EXECUTE'
    )
WHERE r.name = 'CASHIER'
    ON CONFLICT DO NOTHING;

-- WAREHOUSE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r JOIN permissions p ON p.name IN (
                                              'LOCATION_READ', 'PRODUCT_READ', 'PRODUCT_CREATE', 'PRODUCT_UPDATE',
                                              'INVENTORY_READ', 'INVENTORY_ADJUST', 'INVENTORY_TRANSFER'
    )
WHERE r.name = 'WAREHOUSE'
    ON CONFLICT DO NOTHING;

-- ---------- Usuario administrador inicial ----------
WITH new_person AS (
INSERT INTO people (first_name, last_name, document_type, document_number)
VALUES ('Administrador', 'Sistema', 'CC', '0000000000')
    RETURNING id
    ), new_user AS (
INSERT INTO users (person_id, username, email, password_hash)
SELECT id, 'admin', 'admin@cortexpos.local',
    '$2b$10$1V/b.MXLwpIOuXR0wqK4bef20ezko1ia.CCycvxNO1yrofdRKkWDC'
FROM new_person
    RETURNING id
    )
INSERT INTO user_roles (user_id, role_id)
SELECT nu.id, r.id
FROM new_user nu, roles r
WHERE r.name = 'ADMIN';
