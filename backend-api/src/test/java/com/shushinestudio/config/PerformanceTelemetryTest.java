package com.shushinestudio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.QueryStatistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.sql.DataSource;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerformanceTelemetryTest {
    @TempDir Path temp;
    @Test void capturesPoolWaitAndQueryTimingWithoutExportingQueryText() throws Exception {
        var registry=new SimpleMeterRegistry();
        registry.timer("hikaricp.connections.acquire").record(125,TimeUnit.MILLISECONDS);
        var emf=mock(EntityManagerFactory.class);var sf=mock(SessionFactory.class);
        var stats=mock(Statistics.class);var query=mock(QueryStatistics.class);
        when(emf.unwrap(SessionFactory.class)).thenReturn(sf);when(sf.getStatistics()).thenReturn(stats);
        String sensitive="select u from Usuario u where u.login='private@example.invalid'";
        when(stats.getQueries()).thenReturn(new String[]{sensitive});when(stats.getQueryStatistics(sensitive)).thenReturn(query);
        when(query.getExecutionCount()).thenReturn(2L);when(query.getExecutionTotalTime()).thenReturn(75L);
        when(query.getExecutionMaxTime()).thenReturn(50L);
        var output=temp.resolve("telemetry.ndjson");
        var telemetry=new PerformanceTelemetry(registry,emf,mock(DataSource.class),new ObjectMapper(),output.toString());
        try {
            var method=PerformanceTelemetry.class.getDeclaredMethod("sample");method.setAccessible(true);method.invoke(telemetry);
            String raw=Files.readString(output);var row=new ObjectMapper().readTree(raw);
            assertEquals(75,row.path("queries").get(0).path("totalMs").asInt());
            assertEquals(64,row.path("queries").get(0).path("id").asText().length());
            assertTrue(raw.contains("hikaricp.connections.acquire"));
            assertFalse(raw.contains("private@example.invalid"));assertFalse(raw.contains("select u"));
            assertEquals(0.125,row.path("meters").get(0).path("values").path("TOTAL_TIME").asDouble(),0.000001);
        } finally {telemetry.stop();registry.close();}
    }
}
