package com.shushinestudio.repositorios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shushinestudio.dtos.cita.CitaGuardar;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** Booking snapshots and one-statement atomic writes, with database exclusion as arbiter. */
@Repository
public class ReservaJdbcRepository {
    public record ServicioDato(Integer id, String nombre, BigDecimal precioBase, Integer duracionMinutos) {}
    public record Contexto(Integer clienteId, String nombre, String apellido, String telefono,
            Integer estilistaId, String estilistaNombre, Boolean estilistaActivo, List<ServicioDato> servicios) {}
    public record Persistida(Integer id, String codigo) {}
    private record Detalle(Integer id, BigDecimal precio, Integer duracion) {}
    private final NamedParameterJdbcTemplate jdbc;
    private final com.fasterxml.jackson.databind.ObjectReader serviciosReader;
    private final com.fasterxml.jackson.databind.ObjectWriter detallesWriter;
    private final Timer preparation;
    private final Timer write;
    public ReservaJdbcRepository(NamedParameterJdbcTemplate jdbc,ObjectMapper json,MeterRegistry registry) {
        this.jdbc=jdbc;
        this.serviciosReader=json.readerFor(new TypeReference<List<ServicioDato>>() {});
        this.detallesWriter=json.writerFor(json.getTypeFactory().constructCollectionType(List.class,Detalle.class));
        // Warm serialization metadata, never cache prices, clients or booking results.
        try {
            serviciosReader.readValue("[{\"id\":0,\"nombre\":\"\",\"precioBase\":0,\"duracionMinutos\":1}]");
            detallesWriter.writeValueAsString(List.of(new Detalle(0,BigDecimal.ZERO,1)));
        } catch(java.io.IOException ex) {throw new IllegalStateException("No se pudo preparar el formato de reservas");}
        this.preparation=registry.timer("application.booking.preparation");
        this.write=registry.timer("application.booking.write");
    }
    public Optional<Contexto> preparar(CitaGuardar dto,String login) {
        var params=new MapSqlParameterSource().addValue("login",login).addValue("estilista",dto.getEstilistaId(),Types.INTEGER)
            .addValue("servicios",dto.getServicioIds());
        var rows=preparation.record(()->jdbc.query("""
            SELECT c.id_cliente,u.nombre,u.apellido,u.telefono,e.id_estilista,e.nombre_completo,e.activo,
                (SELECT coalesce(jsonb_agg(jsonb_build_object('id',s.id_servicio,'nombre',s.nombre,
                    'precioBase',s.precio_base,'duracionMinutos',s.duracion_minutos) ORDER BY s.id_servicio),'[]'::jsonb)
                 FROM public.servicios s WHERE s.id_servicio IN (:servicios)) AS servicios
            FROM public.usuarios u LEFT JOIN public.clientes c ON c.id_usuario=u.id_usuario
            LEFT JOIN public.estilistas e ON e.id_estilista=:estilista
            WHERE u.login=:login
            """,params,(row,n)->{
                try {
                    return new Contexto(row.getObject("id_cliente",Integer.class),row.getString("nombre"),
                        row.getString("apellido"),row.getString("telefono"),row.getObject("id_estilista",Integer.class),
                        row.getString("nombre_completo"),row.getObject("activo",Boolean.class),
                        serviciosReader.<List<ServicioDato>>readValue(row.getString("servicios")));
                } catch(java.io.IOException ex) {throw new DataRetrievalFailureException("No se pudieron leer los datos de reserva");}
            }));
        return rows.stream().findFirst();
    }
    /** Used inside legacy/walk-in transactions before they acquire other write locks. */
    public void bloquearAgenda(Integer estilistaId, java.time.LocalDate fecha) {
        jdbc.query("SELECT pg_advisory_xact_lock(:estilista, CAST(:fecha AS date) - DATE '2000-01-01')",
            new MapSqlParameterSource().addValue("estilista",estilistaId,Types.INTEGER).addValue("fecha",fecha,Types.DATE),
            (org.springframework.jdbc.core.RowCallbackHandler) row -> {});
    }
    public Persistida insertar(CitaGuardar dto,Contexto context,LocalTime fin,BigDecimal subtotal,BigDecimal iva,BigDecimal total) {
        String detalles;
        try {detalles=detallesWriter.writeValueAsString(context.servicios().stream()
                .map(s->new Detalle(s.id(),s.precioBase(),s.duracionMinutos())).toList());}
        catch(java.io.IOException ex) {throw new DataRetrievalFailureException("No se pudieron preparar los servicios de la reserva");}
        var params=new MapSqlParameterSource().addValue("cliente",context.clienteId(),Types.INTEGER)
            .addValue("estilista",context.estilistaId(),Types.INTEGER).addValue("fecha",dto.getFechaCita(),Types.DATE)
            .addValue("anio",dto.getFechaCita().getYear(),Types.INTEGER).addValue("inicio",dto.getHoraInicio(),Types.TIME)
            .addValue("fin",fin,Types.TIME).addValue("subtotal",subtotal).addValue("iva",iva).addValue("total",total)
            .addValue("metodo",dto.getMetodoPagoPreferente()!=null?dto.getMetodoPagoPreferente():"Efectivo")
            .addValue("notas",dto.getNotasCliente(),Types.VARCHAR).addValue("detalles",detalles);
        // CTE data-modifying statements commit together. An exclusion/FK/detail error
        // rolls back the appointment and every detail; no SELECT-based conflict shortcut.
        return write.record(()->jdbc.queryForObject("""
            WITH turno AS MATERIALIZED (
                SELECT pg_advisory_xact_lock(:estilista, CAST(:fecha AS date) - DATE '2000-01-01')
            ), numero AS MATERIALIZED (SELECT nextval('public.cita_codigo_seq') AS valor FROM turno),
            nueva AS (
                INSERT INTO public.citas(codigo_cita,id_cliente,id_estilista,fecha_cita,hora_inicio,hora_fin,
                    estado,subtotal,descuento_puntos,iva,total,metodo_pago_preferente,estado_pago,
                    es_walkin,notas_cliente,origen)
                SELECT 'SHU-' || CAST(:anio AS text) || '-' ||
                    lpad(valor::text,greatest(4,length(valor::text)),'0'),
                    :cliente,:estilista,:fecha,:inicio,:fin,'Confirmed',:subtotal,0,:iva,:total,:metodo,'Pending',false,:notas,'App'
                FROM numero RETURNING id_cita,codigo_cita
            ), detalles AS (
                INSERT INTO public.cita_servicios(id_cita,id_servicio,precio_aplicado,duracion_minutos)
                SELECT n.id_cita,d.id,d.precio,d.duracion FROM nueva n
                CROSS JOIN jsonb_to_recordset(CAST(:detalles AS jsonb)) AS d(id integer,precio numeric,duracion integer)
                RETURNING id_cita_servicio
            )
            SELECT n.id_cita,n.codigo_cita,(SELECT count(*) FROM detalles) AS cantidad_detalles FROM nueva n
            """,params,(row,n)->new Persistida(row.getInt("id_cita"),row.getString("codigo_cita"))));
    }
}
