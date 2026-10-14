-- =============================================================================
-- EXTENSIÓN ADMINISTRATIVA PARA SALÓN ÚNICO: POS & CONTROL DE INVENTARIO
-- Se integra directamente con el esquema core de Shushine Studio (23 tablas)
-- =============================================================================

-- 1. Tabla de Ventas en Punto de Venta (POS)
CREATE TABLE IF NOT EXISTS public.ventas_pos (
    id_venta SERIAL PRIMARY KEY,
    codigo_ticket VARCHAR(30) UNIQUE NOT NULL DEFAULT ('#TCK-' || TO_CHAR(NOW(), 'YYMM') || '-' || LPAD(FLOOR(RANDOM()*10000)::TEXT, 4, '0')),
    id_cliente INT REFERENCES public.clientes(id_cliente) ON DELETE SET NULL,
    id_cita INT REFERENCES public.citas(id_cita) ON DELETE SET NULL,
    id_estilista INT REFERENCES public.estilistas(id_estilista) ON DELETE SET NULL,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    descuento DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    iva DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL DEFAULT 'Efectivo', -- 'Efectivo', 'Tarjeta POS', 'Transferencia', 'Mixto'
    estado VARCHAR(20) NOT NULL DEFAULT 'Completada',   -- 'Completada', 'Anulada', 'Pendiente'
    notas TEXT,
    fecha_venta TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Detalle de Ítems de la Venta (Servicios realizados o Productos adquiridos)
CREATE TABLE IF NOT EXISTS public.detalle_ventas_pos (
    id_detalle SERIAL PRIMARY KEY,
    id_venta INT NOT NULL REFERENCES public.ventas_pos(id_venta) ON DELETE CASCADE,
    tipo_item VARCHAR(20) NOT NULL CHECK (tipo_item IN ('Servicio', 'Producto')),
    id_servicio INT REFERENCES public.servicios(id_servicio) ON DELETE RESTRICT,
    id_producto INT REFERENCES public.productos(id_producto) ON DELETE RESTRICT,
    cantidad INT NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL
);

-- 3. Kardex / Movimientos de Inventario del Salón
CREATE TABLE IF NOT EXISTS public.movimientos_inventario (
    id_movimiento SERIAL PRIMARY KEY,
    id_producto INT NOT NULL REFERENCES public.productos(id_producto) ON DELETE RESTRICT,
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('Entrada', 'Salida', 'Venta', 'Merma', 'Ajuste')),
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL DEFAULT 0,
    stock_nuevo INT NOT NULL DEFAULT 0,
    motivo VARCHAR(150),
    id_venta INT REFERENCES public.ventas_pos(id_venta) ON DELETE SET NULL,
    fecha_movimiento TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Trigger Automático: Actualizar stock_actual en public.productos al registrar movimiento
CREATE OR REPLACE FUNCTION public.fn_actualizar_stock_producto()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tipo_movimiento IN ('Entrada', 'Ajuste') THEN
        UPDATE public.productos 
        SET stock_actual = stock_actual + NEW.cantidad 
        WHERE id_producto = NEW.id_producto;
    ELSIF NEW.tipo_movimiento IN ('Salida', 'Venta', 'Merma') THEN
        UPDATE public.productos 
        SET stock_actual = GREATEST(0, stock_actual - NEW.cantidad) 
        WHERE id_producto = NEW.id_producto;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_actualizar_stock ON public.movimientos_inventario;
CREATE TRIGGER trg_actualizar_stock
AFTER INSERT ON public.movimientos_inventario
FOR EACH ROW
EXECUTE FUNCTION public.fn_actualizar_stock_producto();

-- 5. Vistas Analíticas para el Panel Web Administrativo
CREATE OR REPLACE VIEW public.vw_citas_hoy AS
SELECT 
    c.id_cita,
    c.codigo_cita,
    COALESCE(u.nombre_completo, c_tbl.nombre_walkin, 'Cliente Sin Nombre') AS cliente_nombre,
    COALESCE(u.telefono, c_tbl.telefono_walkin, 'S/T') AS cliente_telefono,
    e.nombre_completo AS estilista_nombre,
    e.color_agenda AS estilista_color,
    c.fecha_cita,
    c.hora_inicio,
    c.hora_fin,
    c.estado,
    c.total,
    c.metodo_pago_preferente,
    c.estado_pago
FROM public.citas c
LEFT JOIN public.clientes c_tbl ON c.id_cliente = c_tbl.id_cliente
LEFT JOIN public.usuarios u ON c_tbl.id_usuario = u.id_usuario
LEFT JOIN public.estilistas e ON c.id_estilista = e.id_estilista
WHERE c.fecha_cita = CURRENT_DATE
ORDER BY c.hora_inicio ASC;

CREATE OR REPLACE VIEW public.vw_productos_stock_bajo AS
SELECT 
    p.id_producto,
    p.codigo_producto,
    p.nombre,
    p.marca,
    c.nombre AS categoria_nombre,
    p.precio,
    p.stock_actual,
    p.stock_minimo,
    CASE 
        WHEN p.stock_actual = 0 THEN 'Agotado'
        WHEN p.stock_actual <= (p.stock_minimo / 2) THEN 'Crítico'
        WHEN p.stock_actual <= p.stock_minimo THEN 'Bajo'
        ELSE 'Óptimo'
    END AS estado_stock
FROM public.productos p
LEFT JOIN public.categorias c ON p.id_categoria = c.id_categoria
WHERE p.stock_actual <= p.stock_minimo AND p.activo = TRUE
ORDER BY p.stock_actual ASC;
