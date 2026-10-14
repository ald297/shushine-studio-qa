-- =============================================================================
-- ESQUEMA RELACIONAL POSTGRESQL/SUPABASE - SALÓN DE BELLEZA MULTI-SUCURSAL
-- Paleta "Elegancia Serena" · Diseño supervisado por Orquestador Nemotron 3 Ultra
-- =============================================================================

-- EXTENSIONES NECESARIAS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- =============================================================================
-- TIPOS ENUM (Tipos de datos personalizados)
-- =============================================================================
CREATE TYPE appointment_status AS ENUM (
    'pendiente', 'confirmada', 'en_progreso', 'completada', 
    'cancelada', 'no_show', 'reagendada'
);

CREATE TYPE appointment_origin AS ENUM (
    'web', 'telefono', 'presencial', 'app', 'marketplace'
);

CREATE TYPE payment_method AS ENUM (
    'efectivo', 'tarjeta_debito', 'tarjeta_credito', 'transferencia', 
    'credito_salon', 'mixto', 'cheque', 'otro'
);

CREATE TYPE payment_status AS ENUM (
    'aprobado', 'pendiente', 'rechazado', 'reembolsado'
);

CREATE TYPE stock_movement_type AS ENUM (
    'entrada', 'salida', 'transferencia_entrada', 'transferencia_salida', 
    'ajuste', 'merma', 'venta', 'devolucion'
);

CREATE TYPE stock_transfer_status AS ENUM (
    'solicitada', 'en_transito', 'recibida', 'cancelada', 'parcial'
);

CREATE TYPE user_role_type AS ENUM (
    'super_admin', 'admin', 'manager', 'receptionist', 'stylist', 'inventory_manager'
);

CREATE TYPE sale_status AS ENUM (
    'completada', 'anulada', 'pendiente'
);

CREATE TYPE staff_status AS ENUM (
    'activo', 'inactivo', 'vacaciones'
);

CREATE TYPE customer_status AS ENUM (
    'activo', 'inactivo', 'bloqueado'
);

CREATE TYPE loyalty_tier AS ENUM (
    'bronze', 'silver', 'gold', 'platinum'
);

CREATE TYPE branch_status AS ENUM (
    'activo', 'inactivo', 'mantenimiento'
);

CREATE TYPE package_status AS ENUM (
    'activo', 'vencido', 'consumido'
);

-- =============================================================================
-- FUNCIONES DE UTILIDAD Y SEGURIDAD RLS
-- =============================================================================

-- Función para obtener organization_id del JWT actual (Supabase Auth)
CREATE OR REPLACE FUNCTION current_organization_id()
RETURNS uuid AS $$
  SELECT (auth.jwt() ->> 'organization_id')::uuid;
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- Función para obtener user_id actual
CREATE OR REPLACE FUNCTION current_user_id()
RETURNS uuid AS $$
  SELECT auth.uid();
$$ LANGUAGE sql STABLE SECURITY DEFINER;

-- Verifica si el usuario tiene un rol específico (global o en sucursal específica)
CREATE OR REPLACE FUNCTION has_role(
    p_role user_role_type, 
    p_branch_id uuid DEFAULT NULL
)
RETURNS boolean AS $$
DECLARE
    v_org_id uuid := current_organization_id();
    v_user_id uuid := current_user_id();
BEGIN
    IF v_org_id IS NULL OR v_user_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    IF p_branch_id IS NOT NULL THEN
        RETURN EXISTS (
            SELECT 1 FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = v_user_id
              AND ur.organization_id = v_org_id
              AND ur.branch_id = p_branch_id
              AND r.name = p_role
              AND ur.is_active = TRUE
        );
    ELSE
        RETURN EXISTS (
            SELECT 1 FROM user_roles ur
            JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = v_user_id
              AND ur.organization_id = v_org_id
              AND ur.branch_id IS NULL  -- Rol global en la organización
              AND r.name = p_role
              AND ur.is_active = TRUE
        );
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Verifica acceso a sucursal (para estilistas, recepcionistas, etc.)
CREATE OR REPLACE FUNCTION has_branch_access(p_branch_id uuid)
RETURNS boolean AS $$
DECLARE
    v_org_id uuid := current_organization_id();
    v_user_id uuid := current_user_id();
BEGIN
    IF v_org_id IS NULL OR v_user_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Super admin y admin global ven todo
    IF has_role('super_admin') OR has_role('admin') THEN
        RETURN TRUE;
    END IF;
    
    -- Verificar asignación explícita a la sucursal
    RETURN EXISTS (
        SELECT 1 FROM user_roles ur
        WHERE ur.user_id = v_user_id
          AND ur.organization_id = v_org_id
          AND ur.branch_id = p_branch_id
          AND ur.is_active = TRUE
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger genérico para updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- TABLAS PRINCIPALES
-- =============================================================================

-- 1. ORGANIZACIONES (Tenants)
CREATE TABLE organizations (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    logo_url TEXT,
    primary_color VARCHAR(7) DEFAULT '#1A1A2E',
    secondary_color VARCHAR(7) DEFAULT '#D4A574',
    timezone VARCHAR(50) DEFAULT 'America/Mexico_City',
    currency VARCHAR(3) DEFAULT 'MXN',
    settings JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_organizations_updated_at
    BEFORE UPDATE ON organizations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE organizations IS 'Tenants principales - cada cuenta de dueño es una organización';

-- 2. SUCURSALES (Branches)
CREATE TABLE branches (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(20) NOT NULL,  -- Código corto: SC, SN, SS, SO
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(255),
    opening_time TIME NOT NULL DEFAULT '09:00',
    closing_time TIME NOT NULL DEFAULT '20:00',
    timezone VARCHAR(50),
    status branch_status DEFAULT 'activo',
    settings JSONB DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (organization_id, code)
);

CREATE INDEX idx_branches_org ON branches(organization_id);
CREATE INDEX idx_branches_status ON branches(organization_id, status);

CREATE TRIGGER update_branches_updated_at
    BEFORE UPDATE ON branches
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE branches IS 'Locales físicos de cada organización';

-- 3. ROLES (Catálogo de roles)
CREATE TABLE roles (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name user_role_type NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    hierarchy_level INT NOT NULL DEFAULT 0,  -- super_admin=100, admin=80, manager=60, etc.
    permissions JSONB DEFAULT '[]',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

INSERT INTO roles (name, display_name, description, hierarchy_level, permissions) VALUES
('super_admin', 'Super Administrador', 'Acceso total a toda la organización', 100, '["*"]'),
('admin', 'Administrador', 'Gestión completa de la organización', 80, '["manage_branches","manage_staff","manage_services","view_analytics","manage_inventory"]'),
('manager', 'Gerente de Sucursal', 'Gestión operativa de su(s) sucursal(es)', 60, '["manage_appointments","manage_staff_branch","view_reports_branch","manage_inventory_branch"]'),
('receptionist', 'Recepcionista', 'Atención al cliente, agenda y POS', 40, '["manage_appointments","manage_pos","view_clients"]'),
('stylist', 'Estilista/Profesional', 'Ve su agenda y perfil de clientes', 20, '["view_own_appointments","view_own_clients","check_in"]'),
('inventory_manager', 'Gestor de Inventario', 'Control de stock y transferencias', 30, '["manage_inventory","manage_transfers","view_reports_inventory"]');

-- 4. USUARIOS (Perfil público extendido de auth.users)
CREATE TABLE users (
    id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    full_name VARCHAR(255),
    avatar_url TEXT,
    phone VARCHAR(50),
    default_branch_id uuid REFERENCES branches(id) ON DELETE SET NULL,
    preferences JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_users_org ON users(organization_id);
CREATE INDEX idx_users_branch ON users(default_branch_id);

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 5. USER_ROLES (Asignación de roles por sucursal)
CREATE TABLE user_roles (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id uuid NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    branch_id uuid REFERENCES branches(id) ON DELETE CASCADE,  -- NULL = rol global en org
    granted_by uuid REFERENCES users(id) ON DELETE SET NULL,
    granted_at TIMESTAMPTZ DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMPTZ,
    UNIQUE (user_id, role_id, organization_id, branch_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id, organization_id);
CREATE INDEX idx_user_roles_branch ON user_roles(branch_id) WHERE branch_id IS NOT NULL;

-- 6. CATEGORÍAS DE SERVICIOS
CREATE TABLE service_categories (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color_hex VARCHAR(7) DEFAULT '#1A1A2E',
    icon VARCHAR(50) DEFAULT 'ph ph-scissors',
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (organization_id, name)
);

CREATE TRIGGER update_service_categories_updated_at
    BEFORE UPDATE ON service_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 7. SERVICIOS
CREATE TABLE services (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    category_id uuid REFERENCES service_categories(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL DEFAULT 30,
    buffer_before_minutes INT DEFAULT 0,
    buffer_after_minutes INT DEFAULT 0,
    base_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    sale_price DECIMAL(10,2),  -- Precio en oferta
    requires_specific_staff BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    image_url TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_services_org ON services(organization_id);
CREATE INDEX idx_services_category ON services(category_id);
CREATE INDEX idx_services_active ON services(organization_id, is_active);

CREATE TRIGGER update_services_updated_at
    BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE services IS 'Catálogo de servicios ofrecidos por la organización';

-- 8. PROFESIONALES / STAFF
CREATE TABLE staff (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id uuid REFERENCES users(id) ON DELETE SET NULL,  -- Puede no tener login
    branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    specialties TEXT[],  -- Array de especialidades
    bio TEXT,
    avatar_url TEXT,
    commission_percentage DECIMAL(5,2) DEFAULT 0,
    commission_flat DECIMAL(10,2) DEFAULT 0,
    status staff_status DEFAULT 'activo',
    hire_date DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_staff_org ON staff(organization_id);
CREATE INDEX idx_staff_branch ON staff(branch_id);
CREATE INDEX idx_staff_user ON staff(user_id);
CREATE INDEX idx_staff_status ON staff(organization_id, status);

CREATE TRIGGER update_staff_updated_at
    BEFORE UPDATE ON staff
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 9. HORARIOS DE PROFESIONALES (Disponibilidad semanal recurrente)
CREATE TABLE staff_schedules (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    staff_id uuid NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),  -- 0=Domingo
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    schedule_type VARCHAR(20) DEFAULT 'work' CHECK (schedule_type IN ('work', 'break', 'blocked')),
    is_recurring BOOLEAN DEFAULT TRUE,
    valid_from DATE DEFAULT CURRENT_DATE,
    valid_until DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_staff_schedules_staff ON staff_schedules(staff_id);
CREATE INDEX idx_staff_schedules_branch_day ON staff_schedules(branch_id, day_of_week);

CREATE TRIGGER update_staff_schedules_updated_at
    BEFORE UPDATE ON staff_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 10. CLIENTES
CREATE TABLE customers (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    primary_branch_id uuid REFERENCES branches(id) ON DELETE SET NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    birth_date DATE,
    gender VARCHAR(20),
    notes TEXT,
    allergies TEXT,
    preferences JSONB DEFAULT '{}',
    tags TEXT[],  -- Etiquetas: ['vip', 'novia', 'alergia_tinte']
    marketing_source VARCHAR(100),
    first_visit_date DATE,
    status customer_status DEFAULT 'activo',
    -- Fidelidad
    loyalty_points INT DEFAULT 0,
    loyalty_tier loyalty_tier DEFAULT 'bronze',
    total_spent DECIMAL(12,2) DEFAULT 0,
    visit_count INT DEFAULT 0,
    last_visit_at TIMESTAMPTZ,
    next_suggested_visit DATE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_customers_org ON customers(organization_id);
CREATE INDEX idx_customers_branch ON customers(primary_branch_id);
CREATE INDEX idx_customers_email ON customers(organization_id, email) WHERE email IS NOT NULL;
CREATE INDEX idx_customers_phone ON customers(organization_id, phone) WHERE phone IS NOT NULL;
CREATE INDEX idx_customers_name_trgm ON customers USING GIN ((first_name || ' ' || last_name) gin_trgm_ops);
CREATE INDEX idx_customers_loyalty ON customers(organization_id, loyalty_tier);
CREATE INDEX idx_customers_status ON customers(organization_id, status);

CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE customers IS 'Base de clientes de la organización (multi-sucursal)';

-- 11. CITAS / AGENDA (Entidad central)
CREATE TABLE appointments (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    customer_id uuid NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    staff_id uuid NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    service_id uuid NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status appointment_status DEFAULT 'pendiente',
    origin appointment_origin DEFAULT 'presencial',
    client_notes TEXT,
    internal_notes TEXT,
    final_price DECIMAL(10,2),
    discount_applied DECIMAL(10,2) DEFAULT 0,
    payment_method_id uuid,  -- Referencia a payments si ya pagado
    check_in_at TIMESTAMPTZ,
    check_out_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    cancelled_by uuid REFERENCES users(id) ON DELETE SET NULL,
    cancel_reason TEXT,
    created_by uuid REFERENCES users(id) ON DELETE SET NULL,
    updated_by uuid REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índices críticos para performance de agenda
CREATE INDEX idx_appointments_branch_time ON appointments(branch_id, starts_at);
CREATE INDEX idx_appointments_staff_time ON appointments(staff_id, starts_at);
CREATE INDEX idx_appointments_customer_time ON appointments(customer_id, starts_at);
CREATE INDEX idx_appointments_status_time ON appointments(organization_id, status, starts_at);
CREATE INDEX idx_appointments_org_time ON appointments(organization_id, starts_at);
CREATE INDEX idx_appointments_today ON appointments(branch_id, starts_at) 
    WHERE starts_at >= CURRENT_DATE AND starts_at < CURRENT_DATE + INTERVAL '1 day';

CREATE TRIGGER update_appointments_updated_at
    BEFORE UPDATE ON appointments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE appointments IS 'Citas agendadas - tabla central del sistema';

-- 12. CATEGORÍAS DE PRODUCTOS
CREATE TABLE product_categories (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (organization_id, name)
);

-- 13. PRODUCTOS (Catálogo vendible)
CREATE TABLE products (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    category_id uuid REFERENCES product_categories(id) ON DELETE SET NULL,
    supplier_id uuid,  -- Referencia futura a tabla suppliers
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    barcode VARCHAR(100),
    description TEXT,
    sale_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    cost_average DECIMAL(10,2) DEFAULT 0,
    tax_percentage DECIMAL(5,2) DEFAULT 16.00,
    product_type VARCHAR(20) DEFAULT 'producto' CHECK (product_type IN ('producto', 'insumo', 'ambos')),
    min_stock INT DEFAULT 0,
    max_stock INT,
    unit_of_measure VARCHAR(20) DEFAULT 'pza',
    image_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (organization_id, sku)
);

CREATE INDEX idx_products_org ON products(organization_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_barcode ON products(organization_id, barcode) WHERE barcode IS NOT NULL;
CREATE INDEX idx_products_active ON products(organization_id, is_active);

CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 14. STOCK POR SUCURSAL (Inventario real)
CREATE TABLE branch_stock (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity_on_hand DECIMAL(10,3) DEFAULT 0,
    quantity_reserved DECIMAL(10,3) DEFAULT 0,
    last_counted_at TIMESTAMPTZ,
    physical_location VARCHAR(100),  -- Ej: "Estante A-3"
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (branch_id, product_id)
);

-- Columna computada para stock disponible
ALTER TABLE branch_stock 
ADD COLUMN quantity_available DECIMAL(10,3) 
GENERATED ALWAYS AS (quantity_on_hand - quantity_reserved) STORED;

CREATE INDEX idx_branch_stock_branch ON branch_stock(branch_id);
CREATE INDEX idx_branch_stock_product ON branch_stock(product_id);
CREATE INDEX idx_branch_stock_low ON branch_stock(branch_id) 
    WHERE quantity_available <= (
        SELECT min_stock FROM products WHERE products.id = branch_stock.product_id
    );

CREATE TRIGGER update_branch_stock_updated_at
    BEFORE UPDATE ON branch_stock
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE branch_stock IS 'Inventario físico por sucursal - cantidad real disponible';

-- 15. MOVIMIENTOS DE STOCK (Trazabilidad completa)
CREATE TABLE stock_movements (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    movement_type stock_movement_type NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    unit_cost DECIMAL(10,4),
    reference_type VARCHAR(50),  -- 'appointment', 'purchase_order', 'transfer', 'adjustment'
    reference_id uuid,
    notes TEXT,
    created_by uuid REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_stock_movements_org ON stock_movements(organization_id);
CREATE INDEX idx_stock_movements_branch ON stock_movements(branch_id);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_type ON stock_movements(movement_type);
CREATE INDEX idx_stock_movements_ref ON stock_movements(reference_type, reference_id);
CREATE INDEX idx_stock_movements_created ON stock_movements(created_at DESC);

COMMENT ON TABLE stock_movements IS 'Kardex completo - cada movimiento de inventario queda registrado';

-- 16. TRANSFERENCIAS ENTRE SUCURSALES
CREATE TABLE stock_transfers (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    origin_branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    destination_branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    status stock_transfer_status DEFAULT 'solicitada',
    requested_by uuid REFERENCES users(id) ON DELETE SET NULL,
    received_by uuid REFERENCES users(id) ON DELETE SET NULL,
    requested_at TIMESTAMPTZ DEFAULT NOW(),
    shipped_at TIMESTAMPTZ,
    received_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_stock_transfers_org ON stock_transfers(organization_id);
CREATE INDEX idx_stock_transfers_origin ON stock_transfers(origin_branch_id);
CREATE INDEX idx_stock_transfers_dest ON stock_transfers(destination_branch_id);
CREATE INDEX idx_stock_transfers_status ON stock_transfers(status);

CREATE TRIGGER update_stock_transfers_updated_at
    BEFORE UPDATE ON stock_transfers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Detalle de transferencia
CREATE TABLE stock_transfer_items (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    transfer_id uuid NOT NULL REFERENCES stock_transfers(id) ON DELETE CASCADE,
    product_id uuid NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity_requested DECIMAL(10,3) NOT NULL,
    quantity_shipped DECIMAL(10,3) DEFAULT 0,
    quantity_received DECIMAL(10,3) DEFAULT 0,
    notes TEXT
);

-- 17. VENTAS / POS (Tickets)
CREATE TABLE sales (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    customer_id uuid REFERENCES customers(id) ON DELETE SET NULL,
    staff_id uuid REFERENCES staff(id) ON DELETE SET NULL,  -- Vendedor
    appointment_id uuid REFERENCES appointments(id) ON DELETE SET NULL,  -- Venta vinculada a cita
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_total DECIMAL(12,2) DEFAULT 0,
    tax_total DECIMAL(12,2) DEFAULT 0,
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    status sale_status DEFAULT 'completada',
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_sales_org ON sales(organization_id);
CREATE INDEX idx_sales_branch ON sales(branch_id);
CREATE INDEX idx_sales_customer ON sales(customer_id);
CREATE INDEX idx_sales_appointment ON sales(appointment_id);
CREATE INDEX idx_sales_created ON sales(created_at DESC);
CREATE INDEX idx_sales_status ON sales(status);

CREATE TRIGGER update_sales_updated_at
    BEFORE UPDATE ON sales
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 18. LÍNEAS DE VENTA
CREATE TABLE sale_items (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id uuid NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id uuid REFERENCES products(id) ON DELETE SET NULL,
    service_id uuid REFERENCES services(id) ON DELETE SET NULL,
    quantity DECIMAL(10,3) NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) DEFAULT 0,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    line_total DECIMAL(12,2) NOT NULL,
    staff_id uuid REFERENCES staff(id) ON DELETE SET NULL,  -- Profesional que realizó el servicio
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CHECK (
        (product_id IS NOT NULL AND service_id IS NULL) OR
        (product_id IS NULL AND service_id IS NOT NULL)
    )
);

CREATE INDEX idx_sale_items_sale ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product ON sale_items(product_id);
CREATE INDEX idx_sale_items_service ON sale_items(service_id);

-- 19. PAGOS (Métodos de pago por venta)
CREATE TABLE payments (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id uuid NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    method payment_method NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    external_reference VARCHAR(255),  -- ID transacción terminal, transferencia, etc.
    status payment_status DEFAULT 'aprobado',
    processed_at TIMESTAMPTZ,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_payments_sale ON payments(sale_id);
CREATE INDEX idx_payments_method ON payments(method);
CREATE INDEX idx_payments_status ON payments(status);

-- 20. PAQUETES / MEMBRESÍAS
CREATE TABLE packages (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    services_included JSONB NOT NULL DEFAULT '{}',  -- {service_id: quantity}
    validity_days INT NOT NULL DEFAULT 365,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TRIGGER update_packages_updated_at
    BEFORE UPDATE ON packages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 21. PAQUETES COMPRADOS POR CLIENTE
CREATE TABLE customer_packages (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id uuid NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    package_id uuid NOT NULL REFERENCES packages(id) ON DELETE CASCADE,
    sessions_remaining JSONB NOT NULL DEFAULT '{}',  -- {service_id: remaining_qty}
    purchased_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL,
    status package_status DEFAULT 'activo',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_customer_packages_customer ON customer_packages(customer_id);
CREATE INDEX idx_customer_packages_status ON customer_packages(status);
CREATE INDEX idx_customer_packages_expires ON customer_packages(expires_at);

CREATE TRIGGER update_customer_packages_updated_at
    BEFORE UPDATE ON customer_packages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- VISTAS EJECUTIVAS (Para alimentar el Dashboard frontend)
-- =============================================================================

-- Vista: Citas de hoy por sucursal (para "Próximas 24h")
CREATE VIEW vw_appointments_today AS
SELECT 
    a.id,
    a.organization_id,
    a.branch_id,
    b.name AS branch_name,
    b.code AS branch_code,
    a.starts_at AT TIME ZONE b.timezone AS local_start,
    a.ends_at AT TIME ZONE b.timezone AS local_end,
    a.status,
    a.origin,
    c.id AS customer_id,
    c.first_name || ' ' || c.last_name AS customer_name,
    LEFT(c.first_name, 1) || LEFT(c.last_name, 1) AS customer_initials,
    s.id AS staff_id,
    s.first_name || ' ' || s.last_name AS staff_name,
    sv.id AS service_id,
    sv.name AS service_name,
    sv.duration_minutes,
    sv.category_id,
    sc.name AS category_name,
    sc.color_hex AS category_color,
    a.final_price,
    a.check_in_at,
    a.created_at
FROM appointments a
JOIN branches b ON b.id = a.branch_id
JOIN customers c ON c.id = a.customer_id
JOIN staff s ON s.id = a.staff_id
JOIN services sv ON sv.id = a.service_id
LEFT JOIN service_categories sc ON sc.id = sv.category_id
WHERE a.starts_at >= CURRENT_DATE 
  AND a.starts_at < CURRENT_DATE + INTERVAL '1 day'
  AND a.organization_id = current_organization_id()
  AND a.status NOT IN ('cancelada', 'no_show');

COMMENT ON VIEW vw_appointments_today IS 'Citas de hoy para el dashboard - respeta RLS por organization_id';

-- Vista: Stock bajo por sucursal (para "Alertas Stock")
CREATE VIEW vw_branch_stock_low AS
SELECT 
    bs.branch_id,
    b.name AS branch_name,
    b.code AS branch_code,
    p.id AS product_id,
    p.name AS product_name,
    p.sku,
    p.barcode,
    p.min_stock,
    bs.quantity_on_hand,
    bs.quantity_reserved,
    bs.quantity_available,
    p.unit_of_measure,
    CASE 
        WHEN bs.quantity_available <= 0 THEN 'critico'
        WHEN bs.quantity_available <= p.min_stock THEN 'bajo'
        ELSE 'ok'
    END AS severity,
    bs.last_counted_at
FROM branch_stock bs
JOIN branches b ON b.id = bs.branch_id
JOIN products p ON p.id = bs.product_id
WHERE bs.organization_id = current_organization_id()
  AND p.is_active = TRUE
  AND bs.quantity_available <= p.min_stock
  AND b.status = 'activo'
ORDER BY 
    CASE 
        WHEN bs.quantity_available <= 0 THEN 1
        ELSE 2
    END,
    bs.quantity_available ASC;

COMMENT ON VIEW vw_branch_stock_low IS 'Productos con stock igual o menor al mínimo por sucursal';

-- Vista: Horario semanal de staff (para calendario)
CREATE VIEW vw_staff_schedule_week AS
SELECT 
    ss.staff_id,
    s.first_name || ' ' || s.last_name AS staff_name,
    ss.branch_id,
    b.name AS branch_name,
    ss.day_of_week,
    ss.start_time,
    ss.end_time,
    ss.schedule_type,
    ss.valid_from,
    ss.valid_until
FROM staff_schedules ss
JOIN staff s ON s.id = ss.staff_id
JOIN branches b ON b.id = ss.branch_id
WHERE ss.organization_id = current_organization_id()
  AND s.status = 'activo'
  AND b.status = 'activo'
  AND (ss.valid_until IS NULL OR ss.valid_until >= CURRENT_DATE);

-- Vista: KPIs de ventas por sucursal (para "Rendimiento por Sucursal")
CREATE VIEW vw_branch_performance AS
SELECT 
    b.id AS branch_id,
    b.name AS branch_name,
    b.code AS branch_code,
    COUNT(DISTINCT a.id) FILTER (WHERE a.starts_at >= CURRENT_DATE - INTERVAL '7 days' 
        AND a.starts_at < CURRENT_DATE) AS appointments_week,
    COALESCE(SUM(sa.total) FILTER (WHERE sa.created_at >= CURRENT_DATE - INTERVAL '7 days'
        AND sa.created_at < CURRENT_DATE), 0) AS sales_week,
    CASE 
        WHEN COUNT(DISTINCT a.id) FILTER (WHERE a.starts_at >= CURRENT_DATE - INTERVAL '7 days' 
            AND a.starts_at < CURRENT_DATE) > 0
        THEN COALESCE(SUM(sa.total) FILTER (WHERE sa.created_at >= CURRENT_DATE - INTERVAL '7 days'
            AND sa.created_at < CURRENT_DATE), 0) / 
            COUNT(DISTINCT a.id) FILTER (WHERE a.starts_at >= CURRENT_DATE - INTERVAL '7 days' 
            AND a.starts_at < CURRENT_DATE)
        ELSE 0
    END AS avg_ticket_week,
    -- Ocupación aproximada: horas agendadas / horas disponibles
    ROUND(
        COALESCE(SUM(EXTRACT(EPOCH FROM (a.ends_at - a.starts_at))/3600) 
            FILTER (WHERE a.starts_at >= CURRENT_DATE - INTERVAL '7 days' 
                AND a.starts_at < CURRENT_DATE
                AND a.status IN ('confirmada', 'en_progreso', 'completada')), 0)
        / NULLIF(
            SUM(EXTRACT(EPOCH FROM (b.closing_time - b.opening_time))/3600) * 7
            , 0) * 100, 1) AS occupancy_pct
FROM branches b
LEFT JOIN appointments a ON a.branch_id = b.id AND a.organization_id = current_organization_id()
LEFT JOIN sales sa ON sa.branch_id = b.id AND sa.organization_id = current_organization_id()
WHERE b.organization_id = current_organization_id()
  AND b.status = 'activo'
GROUP BY b.id, b.name, b.code, b.opening_time, b.closing_time;

-- Vista: Top servicios de la semana
CREATE VIEW vw_top_services_week AS
SELECT 
    sv.id,
    sv.name,
    sc.name AS category_name,
    sc.color_hex AS category_color,
    COUNT(a.id) AS appointment_count,
    ROUND(COUNT(a.id) * 100.0 / NULLIF(SUM(COUNT(a.id)) OVER (), 0), 1) AS percent_of_total
FROM appointments a
JOIN services sv ON sv.id = a.service_id
LEFT JOIN service_categories sc ON sc.id = sv.category_id
WHERE a.organization_id = current_organization_id()
  AND a.starts_at >= CURRENT_DATE - INTERVAL '7 days'
  AND a.starts_at < CURRENT_DATE
  AND a.status IN ('confirmada', 'en_progreso', 'completada')
GROUP BY sv.id, sv.name, sc.name, sc.color_hex
ORDER BY appointment_count DESC
LIMIT 10;

-- =============================================================================
-- POLÍTICAS RLS (ROW LEVEL SECURITY) - MULTI-TENANT
-- =============================================================================

-- Habilitar RLS en todas las tablas
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;
ALTER TABLE branches ENABLE ROW LEVEL SECURITY;
ALTER TABLE roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE services ENABLE ROW LEVEL SECURITY;
ALTER TABLE staff ENABLE ROW LEVEL SECURITY;
ALTER TABLE staff_schedules ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE branch_stock ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_movements ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_transfers ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_transfer_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE packages ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_packages ENABLE ROW LEVEL SECURITY;

-- =============================================================================
-- POLÍTICAS POR TABLA
-- =============================================================================

-- ORGANIZATIONS: Solo super_admin ve su organización
CREATE POLICY "org_select" ON organizations FOR SELECT
    USING (id = current_organization_id());

CREATE POLICY "org_admin_all" ON organizations FOR ALL
    USING (has_role('super_admin') OR has_role('admin'))
    WITH CHECK (has_role('super_admin') OR has_role('admin'));

-- BRANCHES: Usuarios ven sucursales de su org + acceso por rol
CREATE POLICY "branches_select" ON branches FOR SELECT
    USING (
        organization_id = current_organization_id() 
        AND (has_role('super_admin') OR has_role('admin') OR has_branch_access(id))
    );

CREATE POLICY "branches_insert" ON branches FOR INSERT
    WITH CHECK (
        organization_id = current_organization_id() 
        AND (has_role('super_admin') OR has_role('admin'))
    );

CREATE POLICY "branches_update" ON branches FOR UPDATE
    USING (
        organization_id = current_organization_id() 
        AND (has_role('super_admin') OR has_role('admin') OR has_role('manager', branch_id))
    )
    WITH CHECK (organization_id = current_organization_id());

CREATE POLICY "branches_delete" ON branches FOR DELETE
    USING (
        organization_id = current_organization_id() 
        AND (has_role('super_admin') OR has_role('admin'))
    );

-- ROLES: Solo lectura para todos, escritura solo super_admin/admin
CREATE POLICY "roles_select" ON roles FOR SELECT USING (TRUE);
CREATE POLICY "roles_admin" ON roles FOR ALL
    USING (has_role('super_admin') OR has_role('admin'))
    WITH CHECK (has_role('super_admin') OR has_role('admin'));

-- USERS: Cada usuario ve su perfil + admins ven todos de su org
CREATE POLICY "users_select_own" ON users FOR SELECT
    USING (id = current_user_id() OR has_role('super_admin') OR has_role('admin') OR has_role('manager'));

CREATE POLICY "users_update_own" ON users FOR UPDATE
    USING (id = current_user_id() OR has_role('super_admin') OR has_role('admin'))
    WITH CHECK (organization_id = current_organization_id());

CREATE POLICY "users_admin_all" ON users FOR ALL
    USING (has_role('super_admin') OR has_role('admin'))
    WITH CHECK (has_role('super_admin') OR has_role('admin'));

-- USER_ROLES: Gestión de roles
CREATE POLICY "user_roles_select" ON user_roles FOR SELECT
    USING (organization_id = current_organization_id() 
        AND (user_id = current_user_id() OR has_role('super_admin') OR has_role('admin') OR has_role('manager')));

CREATE POLICY "user_roles_manage" ON user_roles FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin')))
    WITH CHECK (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin')));

-- SERVICE_CATEGORIES
CREATE POLICY "svc_cat_select" ON service_categories FOR SELECT
    USING (organization_id = current_organization_id() AND is_active = TRUE);

CREATE POLICY "svc_cat_manage" ON service_categories FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager')))
    WITH CHECK (organization_id = current_organization_id());

-- SERVICES
CREATE POLICY "services_select" ON services FOR SELECT
    USING (organization_id = current_organization_id() AND is_active = TRUE);

CREATE POLICY "services_manage" ON services FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager')))
    WITH CHECK (organization_id = current_organization_id());

-- STAFF
CREATE POLICY "staff_select" ON staff FOR SELECT
    USING (
        organization_id = current_organization_id() 
        AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist') OR has_branch_access(branch_id))
    );

CREATE POLICY "staff_manage" ON staff FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager', branch_id)))
    WITH CHECK (organization_id = current_organization_id());

-- STAFF_SCHEDULES
CREATE POLICY "staff_sched_select" ON staff_schedules FOR SELECT
    USING (organization_id = current_organization_id() AND has_branch_access(branch_id));

CREATE POLICY "staff_sched_manage" ON staff_schedules FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager', branch_id) OR (has_role('stylist') AND staff_id IN (SELECT id FROM staff WHERE user_id = current_user_id()))))
    WITH CHECK (organization_id = current_organization_id());

-- CUSTOMERS: Todos en org ven clientes, pero estilistas solo los suyos
CREATE POLICY "customers_select" ON customers FOR SELECT
    USING (
        organization_id = current_organization_id() 
        AND (
            has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist') OR has_role('inventory_manager')
            OR (has_role('stylist') AND id IN (
                SELECT DISTINCT customer_id FROM appointments 
                WHERE staff_id IN (SELECT id FROM staff WHERE user_id = current_user_id())
            ))
        )
    );

CREATE POLICY "customers_insert" ON customers FOR INSERT
    WITH CHECK (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist')));

CREATE POLICY "customers_update" ON customers FOR UPDATE
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist')))
    WITH CHECK (organization_id = current_organization_id());

CREATE POLICY "customers_delete" ON customers FOR DELETE
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin')));

-- APPOINTMENTS: Política crítica - estilistas solo ven SUS citas
CREATE POLICY "appointments_select" ON appointments FOR SELECT
    USING (
        organization_id = current_organization_id() 
        AND (
            has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist')
            OR (has_role('stylist') AND staff_id IN (SELECT id FROM staff WHERE user_id = current_user_id()))
            OR (has_branch_access(branch_id) AND has_role('inventory_manager'))  -- Para ver citas vinculadas a ventas
        )
    );

CREATE POLICY "appointments_insert" ON appointments FOR INSERT
    WITH CHECK (
        organization_id = current_organization_id() 
        AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist') OR has_role('stylist'))
    );

CREATE POLICY "appointments_update" ON appointments FOR UPDATE
    USING (
        organization_id = current_organization_id() 
        AND (
            has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist')
            OR (has_role('stylist') AND staff_id IN (SELECT id FROM staff WHERE user_id = current_user_id()))
        )
    )
    WITH CHECK (organization_id = current_organization_id());

CREATE POLICY "appointments_delete" ON appointments FOR DELETE
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin')));

-- PRODUCT_CATEGORIES
CREATE POLICY "prod_cat_select" ON product_categories FOR SELECT
    USING (organization_id = current_organization_id() AND is_active = TRUE);

CREATE POLICY "prod_cat_manage" ON product_categories FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('inventory_manager')))
    WITH CHECK (organization_id = current_organization_id());

-- PRODUCTS
CREATE POLICY "products_select" ON products FOR SELECT
    USING (organization_id = current_organization_id() AND is_active = TRUE);

CREATE POLICY "products_manage" ON products FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('inventory_manager')))
    WITH CHECK (organization_id = current_organization_id());

-- BRANCH_STOCK: Crítico para multi-almacén
CREATE POLICY "branch_stock_select" ON branch_stock FOR SELECT
    USING (
        organization_id = current_organization_id() 
        AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('inventory_manager') OR has_branch_access(branch_id))
    );

CREATE POLICY "branch_stock_update" ON branch_stock FOR UPDATE
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager', branch_id) OR has_role('inventory_manager')))
    WITH CHECK (organization_id = current_organization_id());

CREATE POLICY "branch_stock_insert" ON branch_stock FOR INSERT
    WITH CHECK (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('inventory_manager')));

-- STOCK_MOVEMENTS
CREATE POLICY "stock_mov_select" ON stock_movements FOR SELECT
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('inventory_manager') OR has_branch_access(branch_id)));

CREATE POLICY "stock_mov_insert" ON stock_movements FOR INSERT
    WITH CHECK (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('inventory_manager') OR has_role('receptionist')));

-- STOCK_TRANSFERS
CREATE POLICY "transfers_select" ON stock_transfers FOR SELECT
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('inventory_manager') OR has_branch_access(origin_branch_id) OR has_branch_access(destination_branch_id)));

CREATE POLICY "transfers_manage" ON stock_transfers FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('inventory_manager')))
    WITH CHECK (organization_id = current_organization_id());

-- SALES
CREATE POLICY "sales_select" ON sales FOR SELECT
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist') OR has_branch_access(branch_id)));

CREATE POLICY "sales_insert" ON sales FOR INSERT
    WITH CHECK (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist')));

CREATE POLICY "sales_update" ON sales FOR UPDATE
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager')))
    WITH CHECK (organization_id = current_organization_id());

-- SALE_ITEMS
CREATE POLICY "sale_items_select" ON sale_items FOR SELECT
    USING (EXISTS (SELECT 1 FROM sales s WHERE s.id = sale_items.sale_id AND s.organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist') OR has_branch_access(s.branch_id))));

CREATE POLICY "sale_items_manage" ON sale_items FOR ALL
    USING (EXISTS (SELECT 1 FROM sales s WHERE s.id = sale_items.sale_id AND s.organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist'))))
    WITH CHECK (EXISTS (SELECT 1 FROM sales s WHERE s.id = sale_items.sale_id AND s.organization_id = current_organization_id()));

-- PAYMENTS
CREATE POLICY "payments_select" ON payments FOR SELECT
    USING (EXISTS (SELECT 1 FROM sales s WHERE s.id = payments.sale_id AND s.organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist') OR has_branch_access(s.branch_id))));

CREATE POLICY "payments_manage" ON payments FOR ALL
    USING (EXISTS (SELECT 1 FROM sales s WHERE s.id = payments.sale_id AND s.organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist'))))
    WITH CHECK (EXISTS (SELECT 1 FROM sales s WHERE s.id = payments.sale_id AND s.organization_id = current_organization_id()));

-- PACKAGES
CREATE POLICY "packages_select" ON packages FOR SELECT
    USING (organization_id = current_organization_id() AND is_active = TRUE);

CREATE POLICY "packages_manage" ON packages FOR ALL
    USING (organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager')))
    WITH CHECK (organization_id = current_organization_id());

-- CUSTOMER_PACKAGES
CREATE POLICY "cust_pkg_select" ON customer_packages FOR SELECT
    USING (EXISTS (SELECT 1 FROM customers c WHERE c.id = customer_packages.customer_id AND c.organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist') OR (has_role('stylist') AND c.id IN (SELECT customer_id FROM appointments WHERE staff_id IN (SELECT id FROM staff WHERE user_id = current_user_id()))))));

CREATE POLICY "cust_pkg_manage" ON customer_packages FOR ALL
    USING (EXISTS (SELECT 1 FROM customers c WHERE c.id = customer_packages.customer_id AND c.organization_id = current_organization_id() AND (has_role('super_admin') OR has_role('admin') OR has_role('manager') OR has_role('receptionist'))))
    WITH CHECK (EXISTS (SELECT 1 FROM customers c WHERE c.id = customer_packages.customer_id AND c.organization_id = current_organization_id()));

-- =============================================================================
-- DATOS SEMILLA (SEED DATA) - Roles y categorías base
-- =============================================================================

-- Las categorías de servicio se crean por organización via UI, pero aquí un ejemplo:
-- INSERT INTO service_categories (organization_id, name, description, color_hex, icon, display_order) VALUES
-- (org_id, 'Cortes', 'Servicios de corte de cabello', '#1A1A2E', 'ph ph-scissors', 1),
-- (org_id, 'Color', 'Tintes, mechas y tratamientos de color', '#D4A574', 'ph ph-palette', 2),
-- (org_id, 'Tratamientos', 'Keratina, hidratación, reconstrucción', '#A8C4B8', 'ph ph-sparkle', 3),
-- (org_id, 'Barbería', 'Servicios para caballero', '#2D3E50', 'ph ph-user', 4),
-- (org_id, 'Peinados', 'Eventos, novias, ocasiones especiales', '#E07A5F', 'ph ph-crown', 5);

-- =============================================================================
-- FIN DEL ESQUEMA
-- =============================================================================