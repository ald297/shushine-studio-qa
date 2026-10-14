-- =====================================================================
-- SHUSHINE STUDIO — MIGRACIÓN OFICIAL: MÓDULO DE CHAT CLIENTE ↔ ADMIN
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.conversaciones (
    id_conversacion SERIAL PRIMARY KEY,
    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente) ON DELETE CASCADE,
    id_admin UUID REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_ultimo_mensaje TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_conversacion_cliente UNIQUE (id_cliente)
);

CREATE TABLE IF NOT EXISTS public.mensajes (
    id_mensaje SERIAL PRIMARY KEY,
    id_conversacion INT NOT NULL REFERENCES public.conversaciones(id_conversacion) ON DELETE CASCADE,
    id_remitente UUID NOT NULL REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE,
    contenido VARCHAR(1000) NOT NULL,
    fecha_envio TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices para alto rendimiento y consultas rápidas
CREATE INDEX IF NOT EXISTS idx_conversaciones_cliente ON public.conversaciones(id_cliente);
CREATE INDEX IF NOT EXISTS idx_conversaciones_fecha ON public.conversaciones(fecha_ultimo_mensaje DESC);
CREATE INDEX IF NOT EXISTS idx_mensajes_conversacion ON public.mensajes(id_conversacion);
CREATE INDEX IF NOT EXISTS idx_mensajes_remitente ON public.mensajes(id_remitente);
CREATE INDEX IF NOT EXISTS idx_mensajes_fecha_envio ON public.mensajes(fecha_envio ASC);
CREATE INDEX IF NOT EXISTS idx_mensajes_no_leidos ON public.mensajes(id_conversacion, leido);
