# 📊 Modelo y Diagrama de Base de Datos — Shunshine Studio (PostgreSQL / Supabase)

> **Proyecto:** Shunshine Studio — Plataforma Móvil Integral de Gestión y Reservas para Salón de Belleza  
> **Institución:** Escuela Superior Franciscana Especializada – AGAPE (ESFE AGAPE / MEGATEC)  
> **Carrera:** Técnico en Ingeniería de Desarrollo de Software | **Módulo:** Construcción de APIs Web  
> **Equipo:** Alex Fernando Alfaro Diaz (Backend / Scrum Master) & Camila Antonia Calderon Cortez (Móvil / QA)  
> **Motor de Persistencia:** PostgreSQL 15+ (Supabase) | **ORM:** Spring Data JPA / Hibernate (HikariCP)  
> **Estado:** Modelo Completo Expandido (23 Tablas Relacionales + 6 Buckets de Storage)  

---

## 1. Diagrama Entidad-Relación Integral (Mermaid ER)

Este diagrama es renderizado de forma nativa por visualizadores de Markdown en GitHub, Azure DevOps, VS Code y editores Mermaid:

```mermaid
erDiagram
    %% ==========================================
    %% RELACIONES DE SEGURIDAD Y CLIENTES
    %% ==========================================
    ROLES ||--o{ USUARIOS : "asigna_rol"
    USUARIOS ||--o| CLIENTES : "perfil_cliente_registrado"
    USUARIOS ||--o{ AUDITORIA_LOGS : "ejecuta_cambio"
    
    CLIENTES ||--o{ CITAS : "agenda_cita"
    CLIENTES ||--o{ FAVORITOS : "guarda"
    CLIENTES ||--o{ TRANSACCIONES_PUNTOS : "acumula_o_canjea"
    CLIENTES ||--o{ RESENAS : "emite_opinion"
    
    %% ==========================================
    %% RELACIONES DE CATÁLOGOS Y PRODUCTOS
    %% ==========================================
    CATEGORIAS ||--o{ SERVICIOS : "clasifica_servicios"
    CATEGORIAS ||--o{ PRODUCTOS : "clasifica_productos"
    
    SERVICIOS ||--o{ CITA_SERVICIOS : "incluido_en_cita"
    SERVICIOS ||--o{ ESTILISTA_SERVICIOS : "capacitado_en"
    SERVICIOS ||--o{ SERVICIO_PRODUCTOS_REC : "recomienda_producto"
    PRODUCTOS ||--o{ SERVICIO_PRODUCTOS_REC : "es_recomendado"
    
    %% ==========================================
    %% RELACIONES DE PERSONAL (STAFF NO AUTENTICADO)
    %% ==========================================
    ESTILISTAS ||--o{ ESTILISTA_SERVICIOS : "atiende"
    ESTILISTAS ||--o{ HORARIOS_ESTILISTAS : "turnos_semanales"
    ESTILISTAS ||--o{ BLOQUEOS_HORARIOS : "permisos_vacaciones"
    ESTILISTAS ||--o{ PERSONAL_PORTAFOLIOS : "galeria_trabajos"
    ESTILISTAS ||--o{ CITAS : "atiende_servicio"
    ESTILISTAS ||--o{ RESENAS : "evaluado_en"
    
    %% ==========================================
    %% RELACIONES DE LA CITA COMO NÚCLEO TRANSACCIONAL
    %% ==========================================
    CITAS ||--o{ CITA_SERVICIOS : "desglosa_servicios"
    CITAS ||--o| SOLICITUDES_DISENO : "adjunta_diseno"
    CITAS ||--o{ MENSAJES_CHAT_CITA : "hilo_conversacion"
    CITAS ||--o{ PAGOS : "liquida_cobro"
    CITAS ||--o| FACTURAS : "emite_comprobante"
    CITAS ||--o| RESENAS : "calificada_en"
    CITAS ||--o{ TRANSACCIONES_PUNTOS : "genera_puntos"
    
    SOLICITUDES_DISENO ||--o| COTIZACIONES : "valorada_con"

    %% ==========================================
    %% DEFINICIÓN DE ENTIDADES Y ATRIBUTOS
    %% ==========================================

    ROLES {
        int id_rol PK
        string nombre "Administrador, Cliente"
        string descripcion
    }

    USUARIOS {
        uuid id_usuario PK "FK auth.users(id)"
        int id_rol FK
        string nombre_completo
        string correo UK
        string telefono
        boolean activo
        timestamptz fecha_registro
    }

    CLIENTES {
        int id_cliente PK
        uuid id_usuario FK "Nullable para Walk-in"
        string nombre_walkin "Si no tiene cuenta"
        string telefono_walkin
        date fecha_nacimiento
        string nivel_fidelidad "Bronce, Plata, Oro"
        int puntos_acumulados
        string tipo_cabello
        string notas_preferencias
        boolean es_walkin
        timestamptz fecha_creacion
    }

    CATEGORIAS {
        int id_categoria PK
        string nombre UK "Cabello, Uñas, Pestañas, Spa"
        string descripcion
        string icono_url
        string tipo "Servicio, Producto"
        boolean activo
    }

    SERVICIOS {
        int id_servicio PK
        string codigo_servicio UK "ej. SRV-U01"
        int id_categoria FK
        string nombre
        string descripcion
        decimal precio_base
        boolean es_precio_variable
        int duracion_minutos
        int intervalo_seguimiento_dias "ej. 21 dias"
        string imagen_url
        decimal costo_insumos
        boolean activo
    }

    PRODUCTOS {
        int id_producto PK
        string codigo_producto UK "ej. PRD-SH01"
        int id_categoria FK
        string nombre
        string marca
        string descripcion
        decimal precio
        int stock_actual
        int stock_minimo
        string imagen_url
        boolean activo
    }

    ESTILISTAS {
        int id_estilista PK "Personal No Autenticado"
        string nombre_completo
        string especialidad_principal
        string biografia
        string avatar_url
        string color_agenda
        decimal porcentaje_comision
        boolean activo
    }

    ESTILISTA_SERVICIOS {
        int id_estilista_servicio PK
        int id_estilista FK
        int id_servicio FK
    }

    HORARIOS_ESTILISTAS {
        int id_horario PK
        int id_estilista FK
        int dia_semana "1=Lunes a 7=Domingo"
        time hora_inicio
        time hora_fin
        time hora_inicio_almuerzo
        time hora_fin_almuerzo
        boolean activo
    }

    BLOQUEOS_HORARIOS {
        int id_bloqueo PK
        int id_estilista FK
        date fecha
        time hora_inicio
        time hora_fin
        string motivo "Vacaciones, Incapacidad"
    }

    PERSONAL_PORTAFOLIOS {
        int id_portafolio PK
        int id_estilista FK
        string titulo
        string descripcion
        string imagen_url
        timestamptz fecha_publicacion
    }

    CITAS {
        int id_cita PK
        string codigo_cita UK "ej. #SHU-8492"
        int id_cliente FK
        int id_estilista FK
        date fecha_cita
        time hora_inicio
        time hora_fin
        string estado "PendingQuote, QuoteProposed, Confirmed, InProgress, Completed, Cancelled, NoShow"
        decimal subtotal
        decimal descuento_puntos
        decimal iva "13 porciento"
        decimal total
        string metodo_pago_preferente "Efectivo, Tarjeta POS"
        string estado_pago "Pending, Paid, PartiallyPaid, Refunded"
        boolean es_walkin
        string motivo_cancelacion
        timestamptz fecha_creacion
    }

    CITA_SERVICIOS {
        int id_cita_servicio PK
        int id_cita FK
        int id_servicio FK
        decimal precio_aplicado
        int duracion_minutos
        string notas
    }

    SOLICITUDES_DISENO {
        int id_solicitud PK
        int id_cita FK UK
        jsonb imagenes_referencia_urls "1 a 3 fotos"
        string notas_cliente
        string estado "Pendiente, Cotizado, Aceptado, Rechazado"
        timestamptz fecha_solicitud
    }

    COTIZACIONES {
        int id_cotizacion PK
        int id_solicitud FK UK
        decimal precio_propuesto
        string descripcion_trabajo
        string estado "Propuesta, Aceptada, Rechazada, Expirada"
        timestamptz fecha_cotizacion
        timestamptz fecha_respuesta
    }

    MENSAJES_CHAT_CITA {
        int id_mensaje PK
        int id_cita FK
        uuid id_emisor_usuario FK "Admin o Cliente"
        string remitente_tipo "Cliente, Salon"
        string mensaje
        jsonb imagenes_adjuntas
        boolean es_mensaje_sistema
        boolean leido
        timestamptz fecha_envio
    }

    RESENAS {
        int id_resena PK
        int id_cita FK UK
        int id_cliente FK
        int id_estilista FK
        int estrellas_general "1 a 5"
        int estrellas_calidad "1 a 5"
        int estrellas_atencion "1 a 5"
        int estrellas_ambiente "1 a 5"
        string comentario
        boolean visible_publica
        timestamptz fecha_emision
    }

    TRANSACCIONES_PUNTOS {
        int id_transaccion_puntos PK
        int id_cliente FK
        int id_cita FK
        int puntos "Positivo o Negativo"
        string tipo_movimiento "Acumulacion, Canje, Reembolso"
        string descripcion
        timestamptz fecha_registro
    }

    SERVICIO_PRODUCTOS_REC {
        int id_relacion PK
        int id_servicio FK
        int id_producto FK
        string motivo_recomendacion
    }

    FAVORITOS {
        int id_favorito PK
        int id_cliente FK
        string tipo_entidad "Servicio, Producto, Estilista"
        int id_referencia
        timestamptz fecha_guardado
    }

    PAGOS {
        int id_pago PK
        int id_cita FK
        decimal monto
        string metodo_pago "Efectivo, Tarjeta"
        string estado "Aprobado, Reembolsado"
        string referencia_pos
        string motivo_ajuste_precio
        timestamptz fecha_pago
    }

    FACTURAS {
        int id_factura PK
        int id_cita FK UK
        string numero_factura UK "FAC-2026-XXXXX"
        timestamptz fecha_emision
        decimal subtotal
        decimal iva
        decimal total
        jsonb datos_emisor_receptor
    }

    AUDITORIA_LOGS {
        int id_auditoria PK
        uuid id_usuario FK
        string entidad_afectada "Citas, Precios, Catalogo, Pagos"
        string accion "INSERT, UPDATE, DELETE"
        text valor_anterior
        text valor_nuevo
        string motivo "Requerido en ajustes criticos"
        timestamptz fecha_hora
    }
```

---

## 2. Código DBML (para visualización y exportación en [dbdiagram.io](https://dbdiagram.io/))

> **Instrucciones:**  
> 1. Abre [https://dbdiagram.io/d](https://dbdiagram.io/).  
> 2. Pega el código a continuación para interactuar visualmente con el modelo relacional completo.

```dbml
// ==========================================
// SHUNSHINE STUDIO - MODELO RELACIONAL OFICIAL
// ==========================================

Table Roles {
  id_rol int [pk, increment]
  nombre varchar(50) [not null, unique, note: 'Administrador, Cliente']
  descripcion varchar(200)
}

Table Usuarios {
  id_usuario uuid [pk, note: 'FK directa a auth.users(id)']
  id_rol int [not null, ref: > Roles.id_rol]
  nombre_completo varchar(150) [not null]
  correo varchar(150) [not null, unique]
  telefono varchar(20) [not null]
  activo boolean [not null, default: true]
  fecha_registro timestamptz [not null, default: `now()`]
}

Table Clientes {
  id_cliente int [pk, increment]
  id_usuario uuid [null, unique, ref: - Usuarios.id_usuario, note: 'Nullable para walk-ins']
  nombre_walkin varchar(150) [null]
  telefono_walkin varchar(20) [null]
  fecha_nacimiento date [null]
  nivel_fidelidad varchar(50) [default: 'Bronce', note: 'Bronce, Plata, Oro']
  puntos_acumulados int [not null, default: 0]
  tipo_cabello varchar(100) [null]
  notas_preferencias varchar(500) [null]
  es_walkin boolean [not null, default: false]
  fecha_creacion timestamptz [not null, default: `now()`]
}

Table Categorias {
  id_categoria int [pk, increment]
  nombre varchar(100) [not null, unique]
  descripcion varchar(255)
  icono_url varchar(500)
  tipo varchar(30) [not null, default: 'Servicio', note: 'Servicio, Producto']
  activo boolean [not null, default: true]
}

Table Servicios {
  id_servicio int [pk, increment]
  codigo_servicio varchar(20) [not null, unique]
  id_categoria int [not null, ref: > Categorias.id_categoria]
  nombre varchar(150) [not null]
  descripcion text [not null]
  precio_base decimal(10,2) [not null]
  es_precio_variable boolean [not null, default: false]
  duracion_minutos int [not null]
  intervalo_seguimiento_dias int [not null, default: 21]
  imagen_url varchar(500)
  costo_insumos decimal(10,2) [default: 0.00]
  activo boolean [not null, default: true]
}

Table Productos {
  id_producto int [pk, increment]
  codigo_producto varchar(20) [not null, unique]
  id_categoria int [not null, ref: > Categorias.id_categoria]
  nombre varchar(150) [not null]
  marca varchar(100) [not null]
  descripcion text
  precio decimal(10,2) [not null]
  stock_actual int [not null, default: 0]
  stock_minimo int [not null, default: 5]
  imagen_url varchar(500)
  activo boolean [not null, default: true]
}

Table Estilistas {
  id_estilista int [pk, increment, note: 'Personal No Autenticado']
  nombre_completo varchar(150) [not null]
  especialidad_principal varchar(100) [not null]
  biografia text
  avatar_url varchar(500)
  color_agenda varchar(20) [not null, default: '#D81B60']
  porcentaje_comision decimal(5,2) [not null, default: 0.00]
  activo boolean [not null, default: true]
}

Table EstilistaServicios {
  id_estilista_servicio int [pk, increment]
  id_estilista int [not null, ref: > Estilistas.id_estilista]
  id_servicio int [not null, ref: > Servicios.id_servicio]

  indexes {
    (id_estilista, id_servicio) [unique]
  }
}

Table HorariosEstilistas {
  id_horario int [pk, increment]
  id_estilista int [not null, ref: > Estilistas.id_estilista]
  dia_semana int [not null, note: '1=Lunes a 7=Domingo']
  hora_inicio time [not null]
  hora_fin time [not null]
  hora_inicio_almuerzo time
  hora_fin_almuerzo time
  activo boolean [not null, default: true]
}

Table BloqueosHorarios {
  id_bloqueo int [pk, increment]
  id_estilista int [not null, ref: > Estilistas.id_estilista]
  fecha date [not null]
  hora_inicio time [not null]
  hora_fin time [not null]
  motivo varchar(200) [not null]
}

Table PersonalPortafolios {
  id_portafolio int [pk, increment]
  id_estilista int [not null, ref: > Estilistas.id_estilista]
  titulo varchar(150) [not null]
  descripcion varchar(500)
  imagen_url varchar(500) [not null]
  fecha_publicacion timestamptz [not null, default: `now()`]
}

Table Citas {
  id_cita int [pk, increment]
  codigo_cita varchar(20) [not null, unique]
  id_cliente int [not null, ref: > Clientes.id_cliente]
  id_estilista int [not null, ref: > Estilistas.id_estilista]
  fecha_cita date [not null]
  hora_inicio time [not null]
  hora_fin time [not null]
  estado varchar(30) [not null, default: 'Confirmed', note: 'PendingQuote, QuoteProposed, Confirmed, InProgress, Completed, Cancelled, NoShow']
  subtotal decimal(10,2) [not null]
  descuento_puntos decimal(10,2) [not null, default: 0.00]
  iva decimal(10,2) [not null]
  total decimal(10,2) [not null]
  metodo_pago_preferente varchar(50) [not null, default: 'Efectivo']
  estado_pago varchar(30) [not null, default: 'Pending', note: 'Pending, Paid, PartiallyPaid, Refunded']
  es_walkin boolean [not null, default: false]
  motivo_cancelacion varchar(300)
  fecha_creacion timestamptz [not null, default: `now()`]

  indexes {
    (id_estilista, fecha_cita, hora_inicio) [name: 'idx_estilista_agenda_citas']
  }
}

Table CitaServicios {
  id_cita_servicio int [pk, increment]
  id_cita int [not null, ref: > Citas.id_cita]
  id_servicio int [not null, ref: > Servicios.id_servicio]
  precio_aplicado decimal(10,2) [not null]
  duracion_minutos int [not null]
  notas varchar(255)
}

Table SolicitudesDiseno {
  id_solicitud int [pk, increment]
  id_cita int [not null, unique, ref: - Citas.id_cita]
  imagenes_referencia_urls jsonb [not null, note: 'Array de 1 a 3 URLs de Storage']
  notas_cliente text
  estado varchar(30) [not null, default: 'Pendiente']
  fecha_solicitud timestamptz [not null, default: `now()`]
}

Table Cotizaciones {
  id_cotizacion int [pk, increment]
  id_solicitud int [not null, unique, ref: - SolicitudesDiseno.id_solicitud]
  precio_propuesto decimal(10,2) [not null]
  descripcion_trabajo text [not null]
  estado varchar(30) [not null, default: 'Propuesta', note: 'Propuesta, Aceptada, Rechazada, Expirada']
  fecha_cotizacion timestamptz [not null, default: `now()`]
  fecha_respuesta timestamptz
}

Table MensajesChatCita {
  id_mensaje int [pk, increment]
  id_cita int [not null, ref: > Citas.id_cita]
  id_emisor_usuario uuid [not null, ref: > Usuarios.id_usuario]
  remitente_tipo varchar(20) [not null, note: 'Cliente, Salon']
  mensaje text [not null]
  imagenes_adjuntas jsonb
  es_mensaje_sistema boolean [not null, default: false]
  leido boolean [not null, default: false]
  fecha_envio timestamptz [not null, default: `now()`]
}

Table Resenas {
  id_resena int [pk, increment]
  id_cita int [not null, unique, ref: - Citas.id_cita]
  id_cliente int [not null, ref: > Clientes.id_cliente]
  id_estilista int [not null, ref: > Estilistas.id_estilista]
  estrellas_general int [not null]
  estrellas_calidad int [not null]
  estrellas_atencion int [not null]
  estrellas_ambiente int [not null]
  comentario text
  visible_publica boolean [not null, default: true]
  fecha_emision timestamptz [not null, default: `now()`]
}

Table TransaccionesPuntos {
  id_transaccion_puntos int [pk, increment]
  id_cliente int [not null, ref: > Clientes.id_cliente]
  id_cita int [null, ref: > Citas.id_cita]
  puntos int [not null]
  tipo_movimiento varchar(30) [not null, note: 'Acumulacion, Canje, Reembolso']
  descripcion varchar(200)
  fecha_registro timestamptz [not null, default: `now()`]
}

Table ServicioProductosRec {
  id_relacion int [pk, increment]
  id_servicio int [not null, ref: > Servicios.id_servicio]
  id_producto int [not null, ref: > Productos.id_producto]
  motivo_recomendacion varchar(255)

  indexes {
    (id_servicio, id_producto) [unique]
  }
}

Table Favoritos {
  id_favorito int [pk, increment]
  id_cliente int [not null, ref: > Clientes.id_cliente]
  tipo_entidad varchar(30) [not null, note: 'Servicio, Producto, Estilista']
  id_referencia int [not null]
  fecha_guardado timestamptz [not null, default: `now()`]

  indexes {
    (id_cliente, tipo_entidad, id_referencia) [unique]
  }
}

Table Pagos {
  id_pago int [pk, increment]
  id_cita int [not null, ref: > Citas.id_cita]
  monto decimal(10,2) [not null]
  metodo_pago varchar(50) [not null, note: 'Efectivo, Tarjeta POS']
  estado varchar(30) [not null, default: 'Aprobado', note: 'Aprobado, Reembolsado']
  referencia_pos varchar(100)
  motivo_ajuste_precio varchar(300)
  fecha_pago timestamptz [not null, default: `now()`]
}

Table Facturas {
  id_factura int [pk, increment]
  id_cita int [not null, unique, ref: - Citas.id_cita]
  numero_factura varchar(50) [not null, unique]
  fecha_emision timestamptz [not null, default: `now()`]
  subtotal decimal(10,2) [not null]
  iva decimal(10,2) [not null]
  total decimal(10,2) [not null]
  datos_emisor_receptor jsonb
}

Table AuditoriaLogs {
  id_auditoria int [pk, increment]
  id_usuario uuid [not null, ref: > Usuarios.id_usuario]
  entidad_afectada varchar(50) [not null]
  accion varchar(20) [not null, note: 'INSERT, UPDATE, DELETE']
  valor_anterior text
  valor_nuevo text
  motivo varchar(300) [not null]
  fecha_hora timestamptz [not null, default: `now()`]
}
```

---

## 3. Especificación de Buckets en Supabase Storage y Políticas de Acceso (RLS)

| Bucket | Finalidad | Acceso Lectura | Acceso Escritura | Tamaño Máx. | Formatos |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`servicios-imagenes`** | Fichas ilustrativas del catálogo | Público (`anon`) | Solo Admin (`role = Admin`) | 5 MB | JPG, PNG, WebP |
| **`categorias-imagenes`** | Banners e íconos de categorías | Público (`anon`) | Solo Admin (`role = Admin`) | 2 MB | JPG, PNG, WebP |
| **`personal-avatares`** | Avatares y fotos del personal | Público (`anon`) | Solo Admin (`role = Admin`) | 2 MB | JPG, PNG, WebP |
| **`personal-portafolio`** | Galería de trabajos realizados | Público (`anon`) | Solo Admin (`role = Admin`) | 5 MB | JPG, PNG, WebP |
| **`productos-imagenes`** | Fotos de productos de inventario | Público (`anon`) | Solo Admin (`role = Admin`) | 5 MB | JPG, PNG, WebP |
| **`disenos-referencias`** | Referencias privadas para cotizar | Privado (Propietaria + Admin) | Solo Cliente (`/uid/*`) y Admin | 5 MB | JPG, PNG, WebP |

---

## 4. Script DDL para Supabase (PostgreSQL 15+)

Este script contiene la creación estructurada de las 23 tablas, índices de alta concurrencia, habilitación de RLS, registro de storage buckets, trigger de sincronización de usuarios y datos semilla:

```sql
-- =====================================================================
-- SHUNSHINE STUDIO — SCRIPT DDL OFICIAL DE BASE DE DATOS (POSTGRESQL / SUPABASE)
-- 23 Tablas Relacionales + Triggers + RLS + 6 Buckets de Storage
-- =====================================================================

-- 1. ROLES
CREATE TABLE IF NOT EXISTS public.roles (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
);

-- 2. USUARIOS (Vinculado a auth.users de Supabase)
CREATE TABLE IF NOT EXISTS public.usuarios (
    id_usuario UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    id_rol INT NOT NULL REFERENCES public.roles(id_rol),
    nombre_completo VARCHAR(150) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. CLIENTES (Permite usuarios registrados o clientes walk-in espontáneos)
CREATE TABLE IF NOT EXISTS public.clientes (
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

-- 4. CATEGORIAS (Servicios y Productos)
CREATE TABLE IF NOT EXISTS public.categorias (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    icono_url VARCHAR(500),
    tipo VARCHAR(30) NOT NULL DEFAULT 'Servicio',
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5. SERVICIOS
CREATE TABLE IF NOT EXISTS public.servicios (
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

-- 6. PRODUCTOS (Inventario del Salón)
CREATE TABLE IF NOT EXISTS public.productos (
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

-- 7. ESTILISTAS (Personal del Salón)
CREATE TABLE IF NOT EXISTS public.estilistas (
    id_estilista SERIAL PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    especialidad_principal VARCHAR(100) NOT NULL,
    biografia TEXT,
    avatar_url VARCHAR(500),
    color_agenda VARCHAR(20) NOT NULL DEFAULT '#D81B60',
    porcentaje_comision NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 8. ESTILISTA_SERVICIOS (Matriz de Habilidades)
CREATE TABLE IF NOT EXISTS public.estilista_servicios (
    id_estilista_servicio SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    id_servicio INT NOT NULL REFERENCES public.servicios(id_servicio) ON DELETE CASCADE,
    CONSTRAINT uq_estilista_servicio UNIQUE (id_estilista, id_servicio)
);

-- 9. HORARIOS_ESTILISTAS (Turnos Semanales)
CREATE TABLE IF NOT EXISTS public.horarios_estilistas (
    id_horario SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    dia_semana INT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    hora_inicio_almuerzo TIME,
    hora_fin_almuerzo TIME,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 10. BLOQUEOS_HORARIOS (Permisos, Vacaciones, Incapacidades)
CREATE TABLE IF NOT EXISTS public.bloqueos_horarios (
    id_bloqueo SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    motivo VARCHAR(200) NOT NULL
);

-- 11. PERSONAL_PORTAFOLIOS (Galería de Trabajos)
CREATE TABLE IF NOT EXISTS public.personal_portafolios (
    id_portafolio SERIAL PRIMARY KEY,
    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista) ON DELETE CASCADE,
    titulo VARCHAR(150) NOT NULL,
    descripcion VARCHAR(500),
    imagen_url VARCHAR(500) NOT NULL,
    fecha_publicacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 12. CITAS (Núcleo Transaccional del Salón)
CREATE TABLE IF NOT EXISTS public.citas (
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

-- 13. CITA_SERVICIOS (Desglose de Servicios por Cita)
CREATE TABLE IF NOT EXISTS public.cita_servicios (
    id_cita_servicio SERIAL PRIMARY KEY,
    id_cita INT NOT NULL REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    id_servicio INT NOT NULL REFERENCES public.servicios(id_servicio),
    precio_aplicado NUMERIC(10,2) NOT NULL,
    duracion_minutos INT NOT NULL,
    notas VARCHAR(255)
);

-- 14. SOLICITUDES_DISENO (Diseños Personalizados / Referencias)
CREATE TABLE IF NOT EXISTS public.solicitudes_diseno (
    id_solicitud SERIAL PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    imagenes_referencia_urls JSONB NOT NULL,
    notas_cliente TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
    fecha_solicitud TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 15. COTIZACIONES (Valoración Formal de Solicitudes)
CREATE TABLE IF NOT EXISTS public.cotizaciones (
    id_cotizacion SERIAL PRIMARY KEY,
    id_solicitud INT NOT NULL UNIQUE REFERENCES public.solicitudes_diseno(id_solicitud) ON DELETE CASCADE,
    precio_propuesto NUMERIC(10,2) NOT NULL,
    descripcion_trabajo TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Propuesta' 
        CHECK (estado IN ('Propuesta', 'Aceptada', 'Rechazada', 'Expirada')),
    fecha_cotizacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_respuesta TIMESTAMPTZ
);

-- 16. MENSAJES_CHAT_CITA (Hilo de Negociación y Consulta)
CREATE TABLE IF NOT EXISTS public.mensajes_chat_cita (
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

-- 17. RESENAS (Evaluaciones Post-Servicio)
CREATE TABLE IF NOT EXISTS public.resenas (
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

-- 18. TRANSACCIONES_PUNTOS (Fidelización)
CREATE TABLE IF NOT EXISTS public.transacciones_puntos (
    id_transaccion_puntos SERIAL PRIMARY KEY,
    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente),
    id_cita INT REFERENCES public.citas(id_cita) ON DELETE SET NULL,
    puntos INT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL CHECK (tipo_movimiento IN ('Acumulacion', 'Canje', 'Reembolso', 'AjusteManual')),
    descripcion VARCHAR(200),
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 19. SERVICIO_PRODUCTOS_REC (Cross-selling)
CREATE TABLE IF NOT EXISTS public.servicio_productos_rec (
    id_relacion SERIAL PRIMARY KEY,
    id_servicio INT NOT NULL REFERENCES public.servicios(id_servicio) ON DELETE CASCADE,
    id_producto INT NOT NULL REFERENCES public.productos(id_producto) ON DELETE CASCADE,
    motivo_recomendacion VARCHAR(255),
    CONSTRAINT uq_servicio_producto UNIQUE (id_servicio, id_producto)
);

-- 20. FAVORITOS
CREATE TABLE IF NOT EXISTS public.favoritos (
    id_favorito SERIAL PRIMARY KEY,
    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente) ON DELETE CASCADE,
    tipo_entidad VARCHAR(30) NOT NULL CHECK (tipo_entidad IN ('Servicio', 'Producto', 'Estilista')),
    id_referencia INT NOT NULL,
    fecha_guardado TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cliente_favorito UNIQUE (id_cliente, tipo_entidad, id_referencia)
);

-- 21. PAGOS (Liquidaciones de Caja y POS)
CREATE TABLE IF NOT EXISTS public.pagos (
    id_pago SERIAL PRIMARY KEY,
    id_cita INT NOT NULL REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    monto NUMERIC(10,2) NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'Aprobado' CHECK (estado IN ('Aprobado', 'Reembolsado', 'Anulado')),
    referencia_pos VARCHAR(100),
    motivo_ajuste_precio VARCHAR(300),
    fecha_pago TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 22. FACTURAS (Comprobante Fiscal / Consumidor Final)
CREATE TABLE IF NOT EXISTS public.facturas (
    id_factura SERIAL PRIMARY KEY,
    id_cita INT NOT NULL UNIQUE REFERENCES public.citas(id_cita) ON DELETE CASCADE,
    numero_factura VARCHAR(50) NOT NULL UNIQUE,
    fecha_emision TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    subtotal NUMERIC(10,2) NOT NULL,
    iva NUMERIC(10,2) NOT NULL,
    total NUMERIC(10,2) NOT NULL,
    datos_emisor_receptor JSONB
);

-- 23. AUDITORIA_LOGS (Trazabilidad)
CREATE TABLE IF NOT EXISTS public.auditoria_logs (
    id_auditoria SERIAL PRIMARY KEY,
    id_usuario UUID REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL,
    entidad_afectada VARCHAR(50) NOT NULL,
    accion VARCHAR(20) NOT NULL CHECK (accion IN ('INSERT', 'UPDATE', 'DELETE')),
    valor_anterior TEXT,
    valor_nuevo TEXT,
    motivo VARCHAR(300) NOT NULL,
    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices de optimización
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON public.usuarios(id_rol);
CREATE INDEX IF NOT EXISTS idx_clientes_usuario ON public.clientes(id_usuario);
CREATE INDEX IF NOT EXISTS idx_servicios_categoria ON public.servicios(id_categoria);
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON public.productos(id_categoria);
CREATE INDEX IF NOT EXISTS idx_horarios_estilista ON public.horarios_estilistas(id_estilista, dia_semana);
CREATE INDEX IF NOT EXISTS idx_citas_estilista_agenda ON public.citas(id_estilista, fecha_cita, hora_inicio);
CREATE INDEX IF NOT EXISTS idx_citas_cliente ON public.citas(id_cliente);
CREATE INDEX IF NOT EXISTS idx_cita_servicios_cita ON public.cita_servicios(id_cita);
CREATE INDEX IF NOT EXISTS idx_mensajes_cita ON public.mensajes_chat_cita(id_cita, fecha_envio);
CREATE INDEX IF NOT EXISTS idx_transacciones_cliente ON public.transacciones_puntos(id_cliente);

-- Habilitar Row Level Security (RLS) en todas las tablas
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
```

