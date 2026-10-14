package com.shushinestudio.config;

import com.shushinestudio.modelos.Categoria;
import com.shushinestudio.modelos.Estilista;
import com.shushinestudio.modelos.Servicio;
import com.shushinestudio.repositorios.ICategoriaRepository;
import com.shushinestudio.repositorios.IEstilistaRepository;
import com.shushinestudio.repositorios.IServicioRepository;
import com.shushinestudio.seguridad.modelos.Rol;
import com.shushinestudio.seguridad.modelos.Usuario;
import com.shushinestudio.seguridad.repositorios.RolRepository;
import com.shushinestudio.seguridad.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ICategoriaRepository categoriaRepository;

    @Autowired
    private IServicioRepository servicioRepository;

    @Autowired
    private IEstilistaRepository estilistaRepository;

    @Autowired
    private com.shushinestudio.repositorios.IProductoRepository productoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // 0. Asegurar Existencia de Tablas para Chat en PostgreSQL / Supabase
        if (jdbcTemplate != null) {
            String[] ddlStatements = {
                // Tablas de Chat
                """
                CREATE TABLE IF NOT EXISTS public.conversaciones (
                    id_conversacion SERIAL PRIMARY KEY,
                    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente) ON DELETE CASCADE,
                    id_admin UUID REFERENCES public.usuarios(id_usuario) ON DELETE SET NULL,
                    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    fecha_ultimo_mensaje TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    activa BOOLEAN NOT NULL DEFAULT TRUE,
                    CONSTRAINT uq_conversacion_cliente UNIQUE (id_cliente)
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS public.mensajes (
                    id_mensaje SERIAL PRIMARY KEY,
                    id_conversacion INT NOT NULL REFERENCES public.conversaciones(id_conversacion) ON DELETE CASCADE,
                    id_remitente UUID NOT NULL REFERENCES public.usuarios(id_usuario) ON DELETE CASCADE,
                    contenido VARCHAR(1000) NOT NULL,
                    fecha_envio TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    leido BOOLEAN NOT NULL DEFAULT FALSE,
                    activo BOOLEAN NOT NULL DEFAULT TRUE
                )
                """,
                "CREATE INDEX IF NOT EXISTS idx_conversaciones_cliente ON public.conversaciones(id_cliente)",
                "CREATE INDEX IF NOT EXISTS idx_conversaciones_fecha ON public.conversaciones(fecha_ultimo_mensaje DESC)",
                "CREATE INDEX IF NOT EXISTS idx_mensajes_conversacion ON public.mensajes(id_conversacion)",
                "CREATE INDEX IF NOT EXISTS idx_mensajes_remitente ON public.mensajes(id_remitente)",
                "CREATE INDEX IF NOT EXISTS idx_mensajes_fecha_envio ON public.mensajes(fecha_envio ASC)",
                "CREATE INDEX IF NOT EXISTS idx_mensajes_no_leidos ON public.mensajes(id_conversacion, leido)",
                "ALTER TABLE public.mensajes ADD COLUMN IF NOT EXISTS imagen_url VARCHAR(1000)",

                // Tablas de Reseñas
                """
                CREATE TABLE IF NOT EXISTS public.resenas (
                    id_resena SERIAL PRIMARY KEY,
                    id_cita INT NOT NULL UNIQUE REFERENCES public.citas(id_cita) ON DELETE CASCADE,
                    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente),
                    id_estilista INT NOT NULL REFERENCES public.estilistas(id_estilista),
                    estrellas_general INT NOT NULL CHECK (estrellas_general BETWEEN 1 AND 5),
                    estrellas_calidad INT NOT NULL DEFAULT 5 CHECK (estrellas_calidad BETWEEN 1 AND 5),
                    estrellas_atencion INT NOT NULL DEFAULT 5 CHECK (estrellas_atencion BETWEEN 1 AND 5),
                    estrellas_ambiente INT NOT NULL DEFAULT 5 CHECK (estrellas_ambiente BETWEEN 1 AND 5),
                    comentario TEXT,
                    visible_publica BOOLEAN NOT NULL DEFAULT TRUE,
                    fecha_emision TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """,
                "CREATE INDEX IF NOT EXISTS idx_resenas_cita ON public.resenas(id_cita)",
                "CREATE INDEX IF NOT EXISTS idx_resenas_cliente ON public.resenas(id_cliente)",
                "CREATE INDEX IF NOT EXISTS idx_resenas_estilista ON public.resenas(id_estilista)",

                // Tablas de Productos
                """
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
                )
                """,
                "CREATE INDEX IF NOT EXISTS idx_productos_categoria ON public.productos(id_categoria)",

                // Tablas y columnas de Solicitudes de Diseño y Cotizaciones
                """
                CREATE TABLE IF NOT EXISTS public.solicitudes_diseno (
                    id_solicitud SERIAL PRIMARY KEY,
                    id_cliente INT NOT NULL REFERENCES public.clientes(id_cliente) ON DELETE CASCADE,
                    servicio_deseado VARCHAR(150),
                    notas_cliente TEXT NOT NULL,
                    imagenes_referencia_urls TEXT,
                    estado VARCHAR(30) NOT NULL DEFAULT 'Pendiente',
                    fecha_solicitud TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """,
                "ALTER TABLE public.solicitudes_diseno ADD COLUMN IF NOT EXISTS id_cliente INT REFERENCES public.clientes(id_cliente) ON DELETE CASCADE",
                "ALTER TABLE public.solicitudes_diseno ADD COLUMN IF NOT EXISTS servicio_deseado VARCHAR(150)",
                "ALTER TABLE public.solicitudes_diseno ALTER COLUMN imagenes_referencia_urls TYPE TEXT USING imagenes_referencia_urls::text",
                "ALTER TABLE public.solicitudes_diseno ALTER COLUMN id_cita DROP NOT NULL",
                "ALTER TABLE public.solicitudes_diseno ALTER COLUMN imagenes_referencia_urls DROP NOT NULL",
                "CREATE INDEX IF NOT EXISTS idx_solicitudes_cliente ON public.solicitudes_diseno(id_cliente)",

                """
                CREATE TABLE IF NOT EXISTS public.cotizaciones (
                    id_cotizacion SERIAL PRIMARY KEY,
                    id_solicitud INT NOT NULL UNIQUE REFERENCES public.solicitudes_diseno(id_solicitud) ON DELETE CASCADE,
                    precio_propuesto NUMERIC(10,2) NOT NULL,
                    descripcion_trabajo TEXT NOT NULL,
                    estado VARCHAR(30) NOT NULL DEFAULT 'Propuesta',
                    fecha_cotizacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    fecha_respuesta TIMESTAMPTZ
                )
                """,
                "CREATE INDEX IF NOT EXISTS idx_cotizaciones_solicitud ON public.cotizaciones(id_solicitud)"
            };

            for (String sql : ddlStatements) {
                try {
                    jdbcTemplate.execute(sql.trim());
                } catch (Exception e) {
                    System.err.println("[DataInitializer] Nota en DDL: " + e.getMessage());
                }
            }
        }

        // 1. Inicializar Roles Oficiales del Salón Shushine Studio
        Rol rolAdmin = rolRepository.findByNombreIgnoreCase("ADMIN").orElseGet(() -> {
            Rol nuevoRol = Rol.builder()
                    .nombre("ADMIN")
                    .descripcion("Administrador con acceso integral a gestión, personal y reportes")
                    .build();
            return rolRepository.save(nuevoRol);
        });

        Rol rolCliente = rolRepository.findByNombreIgnoreCase("CLIENTE").orElseGet(() -> {
            Rol nuevoRol = Rol.builder()
                    .nombre("CLIENTE")
                    .descripcion("Cliente registrado para consulta de catálogo y reserva de citas")
                    .build();
            return rolRepository.save(nuevoRol);
        });

        rolRepository.findByNombreIgnoreCase("RECEPCIONISTA").orElseGet(() -> {
            Rol nuevoRol = Rol.builder()
                    .nombre("RECEPCIONISTA")
                    .descripcion("Personal de recepción para atención de citas y cobros")
                    .build();
            return rolRepository.save(nuevoRol);
        });

        // 2. Inicializar Usuario Administrador por Defecto
        if (usuarioRepository.findByLogin("admin").isEmpty()) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .apellido("Shushine")
                    .telefono("70001122")
                    .login("admin")
                    .clave(passwordEncoder.encode("admin123"))
                    .rol(rolAdmin)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
        }

        // 3. Inicializar Usuario Cliente de Prueba
        if (usuarioRepository.findByLogin("cliente").isEmpty()) {
            Usuario cliente = Usuario.builder()
                    .nombre("Camila")
                    .apellido("Calderon")
                    .telefono("70003344")
                    .login("cliente")
                    .clave(passwordEncoder.encode("cliente123"))
                    .rol(rolCliente)
                    .activo(true)
                    .build();
            usuarioRepository.save(cliente);
        }

        // 4. Inicializar Catálogo de Belleza Inicial si está vacío
        if (categoriaRepository.count() == 0) {
            Categoria catCorte = categoriaRepository.save(Categoria.builder()
                    .nombre("Corte y Estilo")
                    .descripcion("Cortes modernos, visagismo y estilismo personalizado para dama y caballero")
                    .iconoUrl("content_cut")
                    .tipo("Servicio")
                    .activo(true)
                    .build());

            Categoria catColor = categoriaRepository.save(Categoria.builder()
                    .nombre("Coloración y Mechas")
                    .descripcion("Balayage, babylights, tintes sin amoniaco y diseño de iluminación capilar")
                    .iconoUrl("brush")
                    .tipo("Servicio")
                    .activo(true)
                    .build());

            Categoria catTratamiento = categoriaRepository.save(Categoria.builder()
                    .nombre("Tratamientos Capilares")
                    .descripcion("Keratina vegetal, botox capilar orgánico y nutrición profunda")
                    .iconoUrl("spa")
                    .tipo("Servicio")
                    .activo(true)
                    .build());

            Categoria catUnas = categoriaRepository.save(Categoria.builder()
                    .nombre("Uñas y Manicura")
                    .descripcion("Manicura rusa, baño de acrílico, soft gel y nail art contemporáneo")
                    .iconoUrl("auto_fix_high")
                    .tipo("Servicio")
                    .activo(true)
                    .build());

            // 5. Inicializar Servicios de Prueba
            servicioRepository.save(Servicio.builder()
                    .codigoServicio("CORTE-01")
                    .categoria(catCorte)
                    .nombre("Corte Dama Signature & Lavado Especial")
                    .descripcion("Lavado con masaje capilar, asesoría de visagismo y corte con acabado secado natural.")
                    .precioBase(new BigDecimal("18.00"))
                    .esPrecioVariable(false)
                    .duracionMinutos(45)
                    .intervaloSeguimientoDias(30)
                    .imagenUrl("https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=600&q=80")
                    .costoInsumos(new BigDecimal("2.50"))
                    .activo(true)
                    .build());

            servicioRepository.save(Servicio.builder()
                    .codigoServicio("COLOR-01")
                    .categoria(catColor)
                    .nombre("Balayage Iluminación & Matiz Glaze")
                    .descripcion("Técnica de aclarado a mano alzada para un efecto de luz degradado sin marcas.")
                    .precioBase(new BigDecimal("65.00"))
                    .esPrecioVariable(true)
                    .duracionMinutos(180)
                    .intervaloSeguimientoDias(60)
                    .imagenUrl("https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80")
                    .costoInsumos(new BigDecimal("15.00"))
                    .activo(true)
                    .build());

            servicioRepository.save(Servicio.builder()
                    .codigoServicio("TRAT-01")
                    .categoria(catTratamiento)
                    .nombre("Hidratación Profunda con Keratina y Ozono")
                    .descripcion("Tratamiento restaurador de puentes de disulfuro para cabellos maltratados o decolorados.")
                    .precioBase(new BigDecimal("35.00"))
                    .esPrecioVariable(false)
                    .duracionMinutos(90)
                    .intervaloSeguimientoDias(21)
                    .imagenUrl("https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?auto=format&fit=crop&w=600&q=80")
                    .costoInsumos(new BigDecimal("8.00"))
                    .activo(true)
                    .build());

            servicioRepository.save(Servicio.builder()
                    .codigoServicio("UNAS-01")
                    .categoria(catUnas)
                    .nombre("Manicura Rusa & Esmaltado Semipermanente")
                    .descripcion("Limpieza milimétrica de cutícula con torno y esmaltado de alta duración.")
                    .precioBase(new BigDecimal("15.00"))
                    .esPrecioVariable(false)
                    .duracionMinutos(60)
                    .intervaloSeguimientoDias(15)
                    .imagenUrl("https://images.unsplash.com/photo-1632345031435-8727f6897d53?auto=format&fit=crop&w=600&q=80")
                    .costoInsumos(new BigDecimal("3.00"))
                    .activo(true)
                    .build());
        }

        // 6. Inicializar Estilistas del Salón si la tabla está vacía
        if (estilistaRepository.count() == 0) {
            estilistaRepository.save(Estilista.builder()
                    .nombreCompleto("Valentina Ramos Cruz")
                    .especialidadPrincipal("Coloración & Balayage")
                    .biografia("Especialista en técnicas de coloración avanzada con 6 años de experiencia. Certificada en balayage iluminado y corrección de color.")
                    .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80")
                    .colorAgenda("#E91E8C")
                    .porcentajeComision(new BigDecimal("35.00"))
                    .activo(true)
                    .build());

            estilistaRepository.save(Estilista.builder()
                    .nombreCompleto("Sofía Martínez Leiva")
                    .especialidadPrincipal("Corte & Estilismo")
                    .biografia("Estilista graduada del Instituto Vidal Sassoon con dominio en cortes de visagismo y secados de alta gama.")
                    .avatarUrl("https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=300&q=80")
                    .colorAgenda("#9C27B0")
                    .porcentajeComision(new BigDecimal("30.00"))
                    .activo(true)
                    .build());

            estilistaRepository.save(Estilista.builder()
                    .nombreCompleto("Andrea López Portillo")
                    .especialidadPrincipal("Uñas & Nail Art")
                    .biografia("Técnica en uñas acrílicas, gel y nail art con formación en Buenos Aires. Especialista en manicura rusa y diseños personalizados.")
                    .avatarUrl("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80")
                    .colorAgenda("#FF5722")
                    .porcentajeComision(new BigDecimal("32.00"))
                    .activo(true)
                    .build());

            estilistaRepository.save(Estilista.builder()
                    .nombreCompleto("Camila Herrera Molina")
                    .especialidadPrincipal("Pestañas & Tratamientos Faciales")
                    .biografia("Certificada en extensiones de pestañas clásicas, volumen ruso y lifting. Experta en limpieza facial profunda y cuidado de la piel.")
                    .avatarUrl("https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=300&q=80")
                    .colorAgenda("#00BCD4")
                    .porcentajeComision(new BigDecimal("30.00"))
                    .activo(true)
                    .build());
        }

        // 7. Inicializar Productos de Belleza si el inventario está vacío
        if (productoRepository.count() == 0) {
            Categoria catTratamiento = categoriaRepository.findAll().stream()
                    .filter(c -> c.getNombre().contains("Tratamiento") || c.getNombre().contains("Capilar"))
                    .findFirst().orElse(null);

            Categoria catColor = categoriaRepository.findAll().stream()
                    .filter(c -> c.getNombre().contains("Color"))
                    .findFirst().orElse(null);

            Categoria catUnas = categoriaRepository.findAll().stream()
                    .filter(c -> c.getNombre().contains("Uñas") || c.getNombre().contains("Manicura"))
                    .findFirst().orElse(null);

            Categoria categoriaDefecto = catTratamiento != null ? catTratamiento : categoriaRepository.findAll().stream().findFirst().orElse(null);

            if (categoriaDefecto != null) {
                productoRepository.save(com.shushinestudio.modelos.Producto.builder()
                        .codigoProducto("PROD-SHAMP-01")
                        .categoria(catTratamiento != null ? catTratamiento : categoriaDefecto)
                        .nombre("Shampoo Reparador Molecular Keratin Glaze 250ml")
                        .marca("Olaplex Professional")
                        .descripcion("Fórmula profesional libre de sulfatos que reconstruye los enlaces de disulfuro capilares.")
                        .precio(new BigDecimal("28.50"))
                        .stockActual(14)
                        .stockMinimo(5)
                        .imagenUrl("https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=600&q=80")
                        .activo(true)
                        .build());

                productoRepository.save(com.shushinestudio.modelos.Producto.builder()
                        .codigoProducto("PROD-MASC-02")
                        .categoria(catTratamiento != null ? catTratamiento : categoriaDefecto)
                        .nombre("Mascarilla Hidratación Intensiva Aceite de Argán & Macadamia 500g")
                        .marca("Moroccanoil")
                        .descripcion("Tratamiento rico en antioxidantes para cabellos secos, decolorados o con procesos químicos.")
                        .precio(new BigDecimal("34.00"))
                        .stockActual(9)
                        .stockMinimo(4)
                        .imagenUrl("https://images.unsplash.com/photo-1608248597359-00e95ff9ef51?auto=format&fit=crop&w=600&q=80")
                        .activo(true)
                        .build());

                productoRepository.save(com.shushinestudio.modelos.Producto.builder()
                        .codigoProducto("PROD-SERUM-03")
                        .categoria(catColor != null ? catColor : categoriaDefecto)
                        .nombre("Sérum Capilar Termoprotector Anti-Frizz Shine Elixir 100ml")
                        .marca("Kérastase")
                        .descripcion("Protección térmica hasta 230°C y sellado de cutícula con brillo espejo.")
                        .precio(new BigDecimal("39.00"))
                        .stockActual(6)
                        .stockMinimo(3)
                        .imagenUrl("https://images.unsplash.com/photo-1526947425960-945c6e72858f?auto=format&fit=crop&w=600&q=80")
                        .activo(true)
                        .build());

                productoRepository.save(com.shushinestudio.modelos.Producto.builder()
                        .codigoProducto("PROD-ESM-04")
                        .categoria(catUnas != null ? catUnas : categoriaDefecto)
                        .nombre("Kit Esmaltado Semipermanente Nude & Rose Gold")
                        .marca("OPI ProSpa")
                        .descripcion("Trío de esmaltes de alta pigmentación y duración de 21 días con acabado profesional.")
                        .precio(new BigDecimal("22.00"))
                        .stockActual(18)
                        .stockMinimo(5)
                        .imagenUrl("https://images.unsplash.com/photo-1632345031435-8727f6897d53?auto=format&fit=crop&w=600&q=80")
                        .activo(true)
                        .build());
            }
        }
    }
}
