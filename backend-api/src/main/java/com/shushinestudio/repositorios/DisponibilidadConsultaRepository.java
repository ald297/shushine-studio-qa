package com.shushinestudio.repositorios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Types;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** One SQL snapshot; independent aggregates avoid multiplying appointments by blocks. */
@Repository
public class DisponibilidadConsultaRepository {
    public record Bloqueo(LocalTime inicio, LocalTime fin, String motivo) {}
    public record Ocupacion(LocalTime inicio, LocalTime fin) {}
    public record Contexto(Integer estilistaId, String nombre, Integer duracionServicio,
            LocalTime inicio, LocalTime fin, LocalTime almuerzoInicio, LocalTime almuerzoFin,
            List<Bloqueo> bloqueos, List<Ocupacion> citas) {}

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper json;
    private final Timer timer;
    public DisponibilidadConsultaRepository(NamedParameterJdbcTemplate jdbc, ObjectMapper json, MeterRegistry registry) {
        this.jdbc=jdbc;this.json=json;this.timer=registry.timer("application.availability.snapshot");
    }
    public Optional<Contexto> consultar(Integer estilistaId, LocalDate fecha, Integer servicioId) {
        var params=new MapSqlParameterSource().addValue("estilista",estilistaId,Types.INTEGER)
                .addValue("fecha",fecha,Types.DATE).addValue("dia",fecha.getDayOfWeek().getValue(),Types.INTEGER)
                .addValue("servicio",servicioId,Types.INTEGER);
        var rows=timer.record(() -> jdbc.query("""
                SELECT e.id_estilista, e.nombre_completo, s.duracion_minutos,
                       h.hora_inicio, h.hora_fin, h.hora_inicio_almuerzo, h.hora_fin_almuerzo,
                       CASE WHEN h.id_horario IS NULL THEN '[]'::jsonb ELSE
                         (SELECT coalesce(jsonb_agg(jsonb_build_object('inicio', b.hora_inicio,
                            'fin', b.hora_fin, 'motivo', b.motivo) ORDER BY b.id_bloqueo), '[]'::jsonb)
                          FROM public.bloqueos_horarios b WHERE b.id_estilista=e.id_estilista AND b.fecha=:fecha)
                       END AS bloqueos,
                       CASE WHEN h.id_horario IS NULL THEN '[]'::jsonb ELSE
                         (SELECT coalesce(jsonb_agg(jsonb_build_object('inicio', c.hora_inicio,
                            'fin', c.hora_fin) ORDER BY c.id_cita), '[]'::jsonb)
                          FROM public.citas c WHERE c.id_estilista=e.id_estilista
                            AND c.fecha_cita=:fecha AND c.estado <> 'Cancelled')
                       END AS citas
                FROM public.estilistas e
                LEFT JOIN public.horarios_estilistas h ON h.id_estilista=e.id_estilista
                    AND h.dia_semana=:dia AND h.activo=true
                LEFT JOIN public.servicios s ON s.id_servicio=:servicio
                WHERE e.id_estilista=:estilista
                """,params,(row,n)->{
                    try {
                        return new Contexto(row.getInt("id_estilista"),row.getString("nombre_completo"),
                            row.getObject("duracion_minutos",Integer.class),
                            row.getObject("hora_inicio",LocalTime.class),row.getObject("hora_fin",LocalTime.class),
                            row.getObject("hora_inicio_almuerzo",LocalTime.class),row.getObject("hora_fin_almuerzo",LocalTime.class),
                            json.readValue(row.getString("bloqueos"),new TypeReference<List<Bloqueo>>() {}),
                            json.readValue(row.getString("citas"),new TypeReference<List<Ocupacion>>() {}));
                    } catch(java.io.IOException ex) {
                        throw new DataRetrievalFailureException("No se pudo leer la disponibilidad");
                    }
                }));
        if(rows.size()>1)throw new DataRetrievalFailureException("Hay horarios activos duplicados para el estilista y día");
        return rows.stream().findFirst();
    }
}
