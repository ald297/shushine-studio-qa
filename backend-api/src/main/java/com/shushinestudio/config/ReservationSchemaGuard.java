package com.shushinestudio.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Evita arrancar READ COMMITTED sin la protección de reservas en PostgreSQL. */
@Component
@DependsOn("entityManagerFactory")
public class ReservationSchemaGuard implements InitializingBean {
    private final JdbcTemplate jdbc;

    public ReservationSchemaGuard(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void afterPropertiesSet() {
        Boolean ready = jdbc.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM pg_constraint c JOIN pg_index i ON i.indexrelid = c.conindid
                    WHERE c.conrelid = 'public.citas'::regclass
                      AND c.conname = 'citas_no_solapamiento_activo'
                      AND c.contype = 'x' AND c.convalidated AND i.indisvalid
                ) AND EXISTS (
                    SELECT 1 FROM pg_constraint
                    WHERE conrelid = 'public.citas'::regclass
                      AND conname = 'citas_horario_valido' AND convalidated
                ) AND to_regclass('public.cita_codigo_seq') IS NOT NULL
                """, Boolean.class);
        if (!Boolean.TRUE.equals(ready)) {
            throw new IllegalStateException("Falta aplicar database/migrations/20261016_reservas_sin_solapamientos.sql antes de iniciar la API.");
        }
    }
}
