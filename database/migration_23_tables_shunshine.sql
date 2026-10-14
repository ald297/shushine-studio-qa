-- =====================================================================
-- SHUNSHINE STUDIO — SCRIPT DDL OFICIAL DE MIGRACIÓN (POSTGRESQL / SUPABASE)
-- Modelo Relacional Expandido: 23 Tablas + Triggers + RLS + Storage Buckets
-- =====================================================================

-- 1. LIMPIEZA DE TABLAS LEGACY O PREVIAS (DROP EN CASCADA ORDENADO)
DROP TABLE IF EXISTS public.auditoria_logs CASCADE;
DROP TABLE IF EXISTS public.facturas CASCADE;
DROP TABLE IF EXISTS public.pagos CASCADE;
DROP TABLE IF EXISTS public.favoritos CASCADE;
DROP TABLE IF EXISTS public.servicio_productos_rec CASCADE;
DROP TABLE IF EXISTS public.transacciones_puntos CASCADE;
DROP TABLE IF EXISTS public.resenas CASCADE;
DROP TABLE IF EXISTS public.mensajes_chat_cita CASCADE;
DROP TABLE IF EXISTS public.cotizaciones CASCADE;
DROP TABLE IF EXISTS public.solicitudes_diseno CASCADE;
DROP TABLE IF EXISTS public.cita_servicios CASCADE;
DROP TABLE IF EXISTS public.citas CASCADE;
DROP TABLE IF EXISTS public.personal_portafolios CASCADE;
DROP TABLE IF EXISTS public.bloqueos_horarios CASCADE;
DROP TABLE IF EXISTS public.horarios_estilistas CASCADE;
DROP TABLE IF EXISTS public.horarios_estilista CASCADE;
DROP TABLE IF EXISTS public.estilista_servicios CASCADE;
DROP TABLE IF EXISTS public.estilistas CASCADE;
DROP TABLE IF EXISTS public.productos CASCADE;
DROP TABLE IF EXISTS public.servicios CASCADE;
DROP TABLE IF EXISTS public.categorias CASCADE;
DROP TABLE IF EXISTS public.categorias_servicio CASCADE;
DROP TABLE IF EXISTS public.clientes CASCADE;
DROP TABLE IF EXISTS public.usuarios CASCADE;
DROP TABLE IF EXISTS public.roles CASCADE;
DROP TABLE IF EXISTS public.reservas CASCADE;
DROP TABLE IF EXISTS public.estaciones_trabajo CASCADE;
DROP TABLE IF EXISTS public.historial_estado_reserva CASCADE;

-- =====================================================================
-- 2. CREACIÓN DE TABLAS RELACIONALES (23 TABLAS)
-- =====================================================================

-- 2.1 ROLES
CREATE TABLE public.roles (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);

-- 2.2 USUARIOS (Vinculado a auth.users de Supabase)
CREATE TABLE public.usuarios (
    id_usuario UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    id_rol INT NOT NULL REFERENCES public.roles(id_rol),
    nombre_completo VARCHAR(150) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.3 CLIENTES (Permite usuarios registrados o clientes walk-in espontáneos)
CREATE TABLE public.clientes (
    id_cliente SERIAL PRIMARY KEY,
    id_usuario UUID UNIQUE REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL,
    nombre_walkin VARCHAR(150),
    telefono_walkin VARCHAR(20),
    fecha_nacimiento DATE,
    nivel_fidelidad VARCHAR(50) NOT NULL DEFAULT 'Bronce',
    puntos_acumulados INT NOT NULL DEFAULT 0,
    tipo_cabello VARCHAR(100),
    notas_preferencias VARCHAR(500),
    es_walkin BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.4 CATEGORIAS (Servicios y Productos)
CREATE TABLE public.categorias (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    icono_url VARCHAR(500),
    tipo VARCHAR(30) NOT NULL DEFAULT 'Servicio',
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2.5 SERVICIOS
CREATE TABLE public.servicios (
    id_servicio SERIAL PRIMARY KEY,
    codigo_servicio VARCHAR(20) NOT NULL UNIQUE,
    id_categoria INT NOT NULL REFERENCES public.categorias(id_categoria),
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT NOT NULL,
    precio_base NUMERIC(10,2) NOT NULL CHECK (precio_base >= 0),
    es_precio_variable BOOLEAN NOT NULL DEFAULT FALSE,
    duracion_minutos INT NOT NULL CHECK (duracion_minutos > 0),
    intervalo_seguimiento_dias INT NOT NULL DEFAULT 21,
    imagen_url VARCHAR(500),
    costo_insumos NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2.6 PRODUCTOS (Inventario del Salón)
CREATE TABLE public.productos (
    id_producto SERIAL PRIMARY KEY,
    codigo_producto VARCHAR(20) NOT NULL UNIQUE,
    id_categoria INT NOT NULL REFERENCES public.categorias(id_categoria),
    nombre VARCHAR(150) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    stock_actual INT NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INT NOT NULL DEFAULT 5,
    imagen_url VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2.7 ESTILISTAS (Personal del Salón)
CREATE TABLE public.estilistas (
    id_estilista SERIAL PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    especialidad_principal VARCHAR(100) NOT NULL,
    biografia TEXT,
    avatar_url VARCHAR(500),
    color_agenda VARCHAR(20) NOT NULL DEFAULT '#D81B60',
    porcentaje_comision NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2.8 ESTILISTA_SERVICIOS (Matriz de Habilidades)
CREATE TABLE public.estilista_servicios (
    id_estilista_servicio SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    id_servicio INT NOT NULL REFERENCES public.servicios(id_servicio) ON DELETE CASCADE,
    CONSTRAINT uq_estilista_servicio UNIQUE (id_estilista, id_servicio)
);

-- 2.9 HORARIOS_ESTILISTAS (Turnos Semanales)
CREATE TABLE public.horarios_estilistas (
    id_horario SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    dia_semana INT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    hora_inicio_almuerzo TIME,
    hora_fin_almuerzo TIME,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2.10 BLOQUEOS_HORARIOS (Permisos, Vacaciones, Incapacidades)
CREATE TABLE public.bloqueos_horarios (
    id_bloqueo SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    motivo VARCHAR(200) NOT NULL
);

-- 2.11 PERSONAL_PORTAFOLIOS (Galería de Trabajos)
CREATE TABLE public.personal_portafolios (
    id_portafolio SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    imagen_url VARCHAR(500) NOT NULL,
    fecha_publicacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.12 CITAS (Núcleo Transaccional del Salón)
CREATE TABLE public.citas (
    id_cita SERIAL PRIMARY KEY,
    codigo_cita VARCHAR(20) NOT NULL UNIQUE,
    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente),
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista),
    fecha_cita DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Confirmed' 
        CHECK (estado IN ('PendingQuote', 'QuoteProposed', 'Confirmed', 'InProgress', 'Completed', 'Cancelled', 'NoShow')),
    subtotal NUMERIC(10,2) NOT NULL,
    descuento_puntos NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    iva NUMERIC(10,2) NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    metodo_pago_preferente VARCHAR(50) NOT NULL DEFAULT 'Efectivo',
    estado_pago VARCHAR(30) NOT NULL DEFAULT 'Pending' 
        CHECK (estado_pago IN ('Pending', 'Paid', 'PartiallyPaid', 'Refunded')),
    es_walkin BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_cancelacion VARCHAR(300),
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.13 CITA_SERVICIOS (Desglose de Servicios por Cita)
CREATE TABLE public.cita_servicios (
    id_cita_servicio SERIAL PRIMARY KEY,
    id_cita INT NOT NULL REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    id_servicio INT NOT NULL REFERENCES public.servicios(id_servicio),
    precio_aplicado NUMERIC(10,2) NOT NULL,
    duracion_minutos INT NOT NULL,
    notas VARCHAR(255)
);

-- 2.14 SOLICITUDES_DISENO (Diseños Personalizados / Referencias)
CREATE TABLE public.solicitudes_diseno (
    id_solicitud SERIAL PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    imagenes_referencia_urls JSONB NOT NULL,
    notas_cliente TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
    fecha_solicitud TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.15 COTIZACIONES (Valoración Formal de Solicitudes)
CREATE TABLE public.cotizaciones (
    id_cotizacion SERIAL PRIMARY KEY,
    id_solicitud INT NOT NULL UNIQUE REFERENCES public.solicitudes_diseno(id_solicitud) ON DELETE CASCADE,
    precio_propuesto NUMERIC(10,2) NOT NULL,
    descripcion_trabajo TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Propuesta' 
        CHECK (estado IN ('Propuesta', 'Aceptada', 'Rechazada', 'Expirada')),
    fecha_cotizacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_respuesta TIMESTAMPTZ
);

-- 2.16 MENSAJES_CHAT_CITA (Hilo de Negociación y Consulta)
CREATE TABLE public.mensajes_chat_cita (
    id_mensaje SERIAL PRIMARY KEY,
    id_cita INT NOT NULL REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    id_emisor_usuario UUID NOT NULL REFERENCES public.usuarios(id_usuario),
    remitente_tipo VARCHAR(20) NOT NULL CHECK (remitente_tipo IN ('Cliente', 'Salon')),
    mensaje TEXT NOT NULL,
    imagenes_adjuntas JSONB,
    es_mensaje_sistema BOOLEAN NOT NULL DEFAULT FALSE,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_envio TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.17 RESENAS (Evaluaciones Post-Servicio)
CREATE TABLE public.resenas (
    id_resena SERIAL PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente),
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista),
    estrellas_general INT NOT NULL CHECK (estrellas_general BETWEEN 1 AND 5),
    estrellas_calidad INT NOT NULL CHECK (estrellas_calidad BETWEEN 1 AND 5),
    estrellas_atencion INT NOT NULL CHECK (estrellas_atencion BETWEEN 1 AND 5),
    estrellas_ambiente INT NOT NULL CHECK (estrellas_ambiente BETWEEN 1 AND 5),
    comentario TEXT,
    visible_publica BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_emision TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.18 TRANSACCIONES_PUNTOS (Fidelización)
CREATE TABLE public.transacciones_puntos (
    id_transaccion_puntos SERIAL PRIMARY KEY,
    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente),
    id_cita INT REFERENCES public.citas(id_cita) ON DELETE SET NULL,
    puntos INT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL CHECK (tipo_movimiento IN ('Acumulacion', 'Canje', 'Reembolso', 'AjusteManual')),
    descripcion VARCHAR(200),
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.19 SERVICIO_PRODUCTOS_REC (Cross-selling y Cuidados Recomendados)
CREATE TABLE public.servicio_productos_rec (
    id_relacion SERIAL PRIMARY KEY,
    id_servicio INT NOT NULL REFERENCES public.servicios(id_servicio) ON DELETE CASCADE,
    id_producto INT NOT NULL REFERENCES public.productos(id_producto) ON DELETE CASCADE,
    motivo_recomendacion VARCHAR(255),
    CONSTRAINT uq_servicio_producto UNIQUE (id_servicio, id_producto)
);

-- 2.20 FAVORITOS
CREATE TABLE public.favoritos (
    id_favorito SERIAL PRIMARY KEY,
    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente) ON DELETE CASCADE,
    tipo_entidad VARCHAR(30) NOT NULL CHECK (tipo_entidad IN ('Servicio', 'Producto', 'Estilista')),
    id_referencia INT NOT NULL,
    fecha_guardado TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cliente_favorito UNIQUE (id_cliente, tipo_entidad, id_referencia)
);

-- 2.21 PAGOS (Liquidaciones de Caja y POS)
CREATE TABLE public.pagos (
    id_pago SERIAL PRIMARY KEY,
    id_cita INT NOT NULL REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    monto NUMERIC(10,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Aprobado' CHECK (estado IN ('Aprobado', 'Reembolsado', 'Anulado')),
    referencia_pos VARCHAR(100),
    motivo_ajuste_precio VARCHAR(300),
    fecha_pago TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2.22 FACTURAS (Comprobante Fiscal / Consumidor Final)
CREATE TABLE public.facturas (
    id_factura SERIAL PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    numero_factura VARCHAR(50) NOT NULL UNIQUE,
    fecha_emision TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    subtotal NUMERIC(10,2) NOT NULL,
    iva NUMERIC(10,2) NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    datos_emisor_receptor JSONB
);

-- 2.23 AUDITORIA_LOGS (Trazabilidad de Modificaciones)
CREATE TABLE public.auditoria_logs (
    id_auditoria SERIAL PRIMARY KEY,
    id_usuario UUID REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL,
    entidad_afectada VARCHAR(50) NOT NULL,
    accion VARCHAR(20) NOT NULL CHECK (accion IN ('INSERT', 'UPDATE', 'DELETE')),
    valor_anterior TEXT,
    valor_nuevo TEXT,
    motivo VARCHAR(300) NOT NULL,
    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =====================================================================
-- 3. ÍNDICES DE RENDIMIENTO PARA ALTA CONCURRENCIA
-- =====================================================================

CREATE INDEX idx_usuarios_rol ON public.usuarios(id_rol);
CREATE INDEX idx_clientes_usuario ON public.clientes(id_usuario);
CREATE INDEX idx_clientes_walkin_tel ON public.clientes(telefono_walkin) WHERE es_walkin = TRUE;
CREATE INDEX idx_servicios_categoria ON public.servicios(id_categoria);
CREATE INDEX idx_productos_categoria ON public.productos(id_categoria);
CREATE INDEX idx_horarios_estilista ON public.horarios_estilistas(id_estilista, dia_semana);
CREATE INDEX idx_bloqueos_estilista_fecha ON public.bloqueos_horarios(id_estilista, fecha);
CREATE INDEX idx_citas_estilista_agenda ON public.citas(id_estilista, fecha_cita, hora_inicio);
CREATE INDEX idx_citas_cliente ON public.citas(id_cliente);
CREATE INDEX idx_citas_estado ON public.citas(estado);
CREATE INDEX idx_cita_servicios_cita ON public.cita_servicios(id_cita);
CREATE INDEX idx_mensajes_cita ON public.mensajes_chat_cita(id_cita, fecha_envio);
CREATE INDEX idx_transacciones_cliente ON public.transacciones_puntos(id_cliente);
CREATE INDEX idx_resenas_estilista ON public.resenas(id_estilista);
CREATE INDEX idx_pagos_cita ON public.pagos(id_cita);

-- =====================================================================
-- 4. ROW LEVEL SECURITY (RLS) EN TODAS LAS TABLAS
-- =====================================================================

ALTER TABLE public.roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.servicios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.estilistas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.estilista_servicios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.horarios_estilistas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bloqueos_horarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.personal_portafolios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.citas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cita_servicios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.solicitudes_diseno ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cotizaciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.mensajes_chat_cita ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.resenas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transacciones_puntos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.servicio_productos_rec ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favoritos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pagos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.facturas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.auditoria_logs ENABLE ROW LEVEL SECURITY;

-- =====================================================================
-- 5. CONFIGURACIÓN DE STORAGE BUCKETS
-- =====================================================================

-- Insertar o actualizar buckets oficiales
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES
    ('servicios-imagenes', 'servicios-imagenes', TRUE, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('categorias-imagenes', 'categorias-imagenes', TRUE, 2097152, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('personal-avatares', 'personal-avatares', TRUE, 2097152, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('personal-portafolio', 'personal-portafolio', TRUE, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('productos-imagenes', 'productos-imagenes', TRUE, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('disenos-referencias', 'disenos-referencias', FALSE, 5242880, ARRAY['image/jpeg', 'image/png', 'image/webp'])
ON CONFLICT (id) DO UPDATE SET
    public = EXCLUDED.public,
    file_size_limit = EXCLUDED.file_size_limit,
    allowed_mime_types = EXCLUDED.allowed_mime_types;

-- Políticas de lectura pública para buckets abiertos
CREATE POLICY "Public Read Access servicios-imagenes" ON storage.objects
    FOR SELECT TO public USING (bucket_id = 'servicios-imagenes');

CREATE POLICY "Public Read Access categorias-imagenes" ON storage.objects
    FOR SELECT TO public USING (bucket_id = 'categorias-imagenes');

CREATE POLICY "Public Read Access personal-avatares" ON storage.objects
    FOR SELECT TO public USING (bucket_id = 'personal-avatares');

CREATE POLICY "Public Read Access personal-portafolio" ON storage.objects
    FOR SELECT TO public USING (bucket_id = 'personal-portafolio');

CREATE POLICY "Public Read Access productos-imagenes" ON storage.objects
    FOR SELECT TO public USING (bucket_id = 'productos-imagenes');

-- Política para diseños privados (Solo el usuario autenticado propietario de la carpeta y admins)
CREATE POLICY "Authenticated Upload disenos-referencias" ON storage.objects
    FOR INSERT TO authenticated WITH CHECK (
        bucket_id = 'disenos-referencias' AND 
        (storage.foldername(name))[1] = auth.uid()::text
    );

CREATE POLICY "Owner Read disenos-referencias" ON storage.objects
    FOR SELECT TO authenticated USING (
        bucket_id = 'disenos-referencias' AND 
        (storage.foldername(name))[1] = auth.uid()::text
    );

-- =====================================================================
-- 6. TRIGGERS Y FUNCIONES DE AUTENTICACIÓN
-- =====================================================================

-- 6.1 Trigger para registro automático de usuarios y vinculación de clientes walk-in
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER 
SECURITY DEFINER
SET search_path = public
LANGUAGE plpgsql
AS $$
DECLARE
    v_id_rol_cliente INT;
    v_nombre_completo VARCHAR(150);
    v_telefono VARCHAR(20);
    v_cliente_existente_id INT;
BEGIN
    -- Obtener el ID del rol 'Cliente'
    SELECT id_rol INTO v_id_rol_cliente FROM public.roles WHERE nombre = 'Cliente' LIMIT 1;
    
    -- Extraer metadatos de auth.users
    v_nombre_completo := COALESCE(NEW.raw_user_meta_data->>'nombre_completo', split_part(NEW.email, '@', 1));
    v_telefono := COALESCE(NEW.raw_user_meta_data->>'telefono', '');

    -- Insertar en public.usuarios con id_usuario = NEW.id (UUID)
    INSERT INTO public.usuarios (
        id_usuario,
        id_rol,
        nombre_completo,
        correo,
        telefono,
        activo,
        fecha_registro
    )
    VALUES (
        NEW.id,
        v_id_rol_cliente,
        v_nombre_completo,
        NEW.email,
        v_telefono,
        TRUE,
        NOW()
    );

    -- Verificar si ya existía un cliente walk-in con el mismo teléfono para asociarlo retrospectivamente
    IF v_telefono <> '' THEN
        SELECT id_cliente INTO v_cliente_existente_id 
        FROM public.clientes 
        WHERE telefono_walkin = v_telefono AND id_usuario IS NULL 
        LIMIT 1;
    END IF;

    IF v_cliente_existente_id IS NOT NULL THEN
        -- Vincular cliente walk-in existente a la nueva cuenta
        UPDATE public.clientes 
        SET id_usuario = NEW.id,
            es_walkin = FALSE
        WHERE id_cliente = v_cliente_existente_id;
    ELSE
        -- Crear nuevo perfil de cliente
        INSERT INTO public.clientes (
            id_usuario,
            nivel_fidelidad,
            puntos_acumulados,
            es_walkin,
            fecha_creacion
        )
        VALUES (
            NEW.id,
            'Bronce',
            0,
            FALSE,
            NOW()
        );
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 6.2 Custom Access Token Hook para inyección de roles en claims JWT
CREATE OR REPLACE FUNCTION public.custom_access_token_hook(event jsonb)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    claims jsonb;
    user_role text;
BEGIN
    SELECT r.nombre
    INTO user_role
    FROM public.usuarios u
    JOIN public.roles r ON u.id_rol = r.id_rol
    WHERE u.id_usuario = (event->>'user_id')::uuid;

    claims := event->'claims';

    IF user_role IS NOT NULL THEN
        claims := jsonb_set(claims, '{user_role}', to_jsonb(user_role));
        claims := jsonb_set(claims, '{role}', to_jsonb(user_role));
    END IF;

    event := jsonb_set(event, '{claims}', claims);
    RETURN event;
END;
$$;

GRANT USAGE ON SCHEMA public TO supabase_auth_admin;
GRANT EXECUTE ON FUNCTION public.custom_access_token_hook TO supabase_auth_admin;
REVOKE EXECUTE ON FUNCTION public.custom_access_token_hook FROM authenticated, anon, public;

-- =====================================================================
-- 7. DATOS SEMILLA INICIALES (SEEDS)
-- =====================================================================

-- 7.1 Roles
INSERT INTO public.roles (id_rol, nombre, descripcion) VALUES
(1, 'Cliente', 'Cliente del salón con acceso móvil a reservas y catálogo'),
(2, 'Administrador', 'Administrador con control total del salón, agenda, cotizaciones y caja'),
(3, 'Recepcionista', 'Personal de recepción para gestión de citas, caja y walk-ins')
ON CONFLICT (id_rol) DO NOTHING;

-- 7.2 Categorías
INSERT INTO public.categorias (id_categoria, nombre, descripcion, icono_url, tipo, activo) VALUES
(1, 'Cabello', 'Tratamientos capilares, corte, tinte, alisados y estilismo', 'https://images.unsplash.com/photo-1560869713-7d0a29430803', 'Servicio', TRUE),
(2, 'Uñas', 'Manicura, pedicura, acrílico, gel y diseño personalizado', 'https://images.unsplash.com/photo-1604654894610-df63bc536371', 'Servicio', TRUE),
(3, 'Pestañas & Cejas', 'Extensiones de pestañas, lifting, laminado y perfilado de cejas', 'https://images.unsplash.com/photo-1583001931096-959e9a1a6223', 'Servicio', TRUE),
(4, 'Spa & Facial', 'Limpiezas faciales, masajes relajantes y tratamientos corporales', 'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881', 'Servicio', TRUE),
(5, 'Cuidado Capilar', 'Shampoos, mascarillas, aceites y tratamientos profesionales', 'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d', 'Producto', TRUE),
(6, 'Cuidado de Uñas & Piel', 'Esmaltes, cremas hidratantes y kits de cuidado', 'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e', 'Producto', TRUE)
ON CONFLICT (id_categoria) DO NOTHING;

-- 7.3 Estilistas
INSERT INTO public.estilistas (id_estilista, nombre_completo, especialidad_principal, biografia, avatar_url, color_agenda, porcentaje_comision, activo) VALUES
(1, 'Valeria Morales', 'Colorista Senior & Estilismo Capilar', 'Especialista en balayage, cambios de look y tratamientos reconstructivos de keratina.', 'https://images.unsplash.com/photo-1595956553066-fe24a8c33395', '#E91E63', 15.00, TRUE),
(2, 'Camila Navarro', 'Nail Artist Master & Diseños de Vanguardia', 'Experta en estructura acrílica, mano alzada y tendencias coreanas/3D.', 'https://images.unsplash.com/photo-1580489944761-15a19d654956', '#9C27B0', 12.00, TRUE),
(3, 'Sofía Hernández', 'Lash & Brow Designer', 'Certificada en volumen ruso, efecto foxy eyes y laminado orgánico.', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb', '#009688', 12.00, TRUE)
ON CONFLICT (id_estilista) DO NOTHING;

-- 7.4 Servicios
INSERT INTO public.servicios (id_servicio, codigo_servicio, id_categoria, nombre, descripcion, precio_base, es_precio_variable, duracion_minutos, intervalo_seguimiento_dias, imagen_url, costo_insumos, activo) VALUES
(1, 'SRV-CAP-01', 1, 'Corte de Cabello & Brushing Premium', 'Corte personalizado según visagismo, lavado con shampoo nutritivo y secado profesional.', 18.00, FALSE, 45, 30, 'https://images.unsplash.com/photo-1560869713-7d0a29430803', 3.00, TRUE),
(2, 'SRV-CAP-02', 1, 'Balayage Iluminado + Matiz', 'Técnica de aclaración degradada a mano alzada, incluye matiz y tratamiento reconstructor.', 85.00, TRUE, 180, 60, 'https://images.unsplash.com/photo-1562322140-8baeececf3df', 25.00, TRUE),
(3, 'SRV-UNA-01', 2, 'Manicura Rusa & Gel Semipermanente', 'Limpieza profunda de cutícula con torno, esmaltado semipermanente de alta duración.', 22.00, FALSE, 60, 21, 'https://images.unsplash.com/photo-1604654894610-df63bc536371', 4.00, TRUE),
(4, 'SRV-UNA-02', 2, 'Uñas Acrílicas Esculpidas con Diseño', 'Set completo de acrílico con opción a diseño personalizado bajo cotización.', 35.00, TRUE, 120, 21, 'https://images.unsplash.com/photo-1632345031435-8727f6897d53', 8.00, TRUE),
(5, 'SRV-PES-01', 3, 'Extensiones de Pestañas Clásicas 1x1', 'Aplicación pelo a pelo para un efecto natural y mirada abierta.', 30.00, FALSE, 90, 21, 'https://images.unsplash.com/photo-1583001931096-959e9a1a6223', 6.00, TRUE),
(6, 'SRV-SPA-01', 4, 'Limpieza Facial Profunda Hidratante', 'Exfoliación, vaporización, extracción con ultrasonido y mascarilla hidroplástica.', 35.00, FALSE, 60, 30, 'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881', 7.00, TRUE)
ON CONFLICT (id_servicio) DO NOTHING;

-- 7.5 Productos
INSERT INTO public.productos (id_producto, codigo_producto, id_categoria, nombre, marca, descripcion, precio, stock_actual, stock_minimo, imagen_url, activo) VALUES
(1, 'PRD-CAP-01', 5, 'Shampoo Reparador de Keratina 500ml', 'Olaplex / L''Oréal Professionnel', 'Limpia suavemente mientras repara enlaces capilares dañados.', 24.50, 15, 3, 'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d', TRUE),
(2, 'PRD-CAP-02', 5, 'Aceite de Argán & Macadamia 100ml', 'Moroccanoil', 'Aporta brillo instantáneo, sella puntas abiertas y controla el frizz.', 28.00, 10, 2, 'https://images.unsplash.com/photo-1608248597358-1f19b2a75d50', TRUE),
(3, 'PRD-UNA-01', 6, 'Aceite Nutritivo para Cutícula con Vitamina E', 'OPI ProSpa', 'Fórmula de rápida absorción que acondiciona y protege cutículas secas.', 12.00, 20, 5, 'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e', TRUE)
ON CONFLICT (id_producto) DO NOTHING;

-- 7.6 Estilista Servicios
INSERT INTO public.estilista_servicios (id_estilista, id_servicio) VALUES
(1, 1), -- Valeria -> Corte
(1, 2), -- Valeria -> Balayage
(2, 3), -- Camila -> Manicura Rusa
(2, 4), -- Camila -> Uñas Acrílicas
(3, 5), -- Sofía -> Pestañas
(1, 6)  -- Valeria -> Facial
ON CONFLICT (id_estilista, id_servicio) DO NOTHING;

-- 7.7 Horarios Semanales (Lunes a Sábado: 09:00 a 18:00, Almuerzo: 13:00 a 14:00)
INSERT INTO public.horarios_estilistas (id_estilista, dia_semana, hora_inicio, hora_fin, hora_inicio_almuerzo, hora_fin_almuerzo, activo)
SELECT e.id_estilista, d.dia, '09:00:00'::TIME, '18:00:00'::TIME, '13:00:00'::TIME, '14:00:00'::TIME, TRUE
FROM public.estilistas e
CROSS JOIN (SELECT generate_series(1, 6) AS dia) d
ON CONFLICT DO NOTHING;

-- 7.8 Recomendaciones Servicio -> Producto (Cross-selling)
INSERT INTO public.servicio_productos_rec (id_servicio, id_producto, motivo_recomendacion) VALUES
(2, 1, 'Recomendado para mantener el tono y nutrir el cabello tras decoloración.'),
(2, 2, 'Ideal para sellar puntas y evitar resequedad por calor o tinte.'),
(3, 3, 'Prolonga el acabado brillante y la hidratación de la manicura.')
ON CONFLICT (id_servicio, id_producto) DO NOTHING;

-- Sincronizar secuencias seriales
SELECT setval('public.roles_id_rol_seq', (SELECT COALESCE(MAX(id_rol), 1) FROM public.roles));
SELECT setval('public.categorias_id_categoria_seq', (SELECT COALESCE(MAX(id_categoria), 1) FROM public.categorias));
SELECT setval('public.estilistas_id_estilista_seq', (SELECT COALESCE(MAX(id_estilista), 1) FROM public.estilistas));
SELECT setval('public.servicios_id_servicio_seq', (SELECT COALESCE(MAX(id_servicio), 1) FROM public.servicios));
SELECT setval('public.productos_id_producto_seq', (SELECT COALESCE(MAX(id_producto), 1) FROM public.productos));
