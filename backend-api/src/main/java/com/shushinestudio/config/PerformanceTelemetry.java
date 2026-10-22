package com.shushinestudio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** Opt-in local evidence. Never records SQL parameters, tokens or request bodies. */
@Component
@ConditionalOnProperty(name="performance.telemetry.enabled", havingValue="true")
public class PerformanceTelemetry {
    private final MeterRegistry registry;
    private final EntityManagerFactory emf;
    private final DataSource source;
    private final ObjectMapper json;
    private final Path output;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "performance-telemetry"); thread.setDaemon(true); return thread;
    });
    public PerformanceTelemetry(MeterRegistry registry, EntityManagerFactory emf, DataSource source,
            ObjectMapper json, @Value("${performance.telemetry.file}") String output) {
        this.registry=registry; this.emf=emf; this.source=source; this.json=json; this.output=Path.of(output);
    }
    @EventListener(ApplicationReadyEvent.class)
    public void start() throws Exception {
        Files.createDirectories(output.toAbsolutePath().getParent());
        executor.scheduleWithFixedDelay(this::sample, 0, 2, TimeUnit.SECONDS);
    }
    private void sample() {
        try {
            Map<String,Object> row = new LinkedHashMap<>();
            row.put("time", Instant.now().toString());
            List<Object> meters=new ArrayList<>();
            registry.getMeters().forEach(m -> {
                String name=m.getId().getName();
                if (!(name.startsWith("hikaricp.") || name.startsWith("jvm.") || name.startsWith("process.")
                        || name.startsWith("system.") || name.equals("http.server.requests") || name.equals("application.availability.snapshot") || name.startsWith("application.booking."))) return;
                Map<String,Object> entry=new LinkedHashMap<>(); entry.put("name",name);
                Map<String,String> tags=new TreeMap<>(); m.getId().getTags().forEach(t -> tags.put(t.getKey(),t.getValue()));
                entry.put("tags",tags); Map<String,Double> values=new TreeMap<>();
                m.measure().forEach(v -> {if(Double.isFinite(v.getValue())) values.put(v.getStatistic().toString(),v.getValue());});
                entry.put("values",values); meters.add(entry);
            });
            row.put("meters",meters);
            if(source instanceof HikariDataSource hikari && hikari.getHikariPoolMXBean()!=null) {
                var pool=hikari.getHikariPoolMXBean();
                row.put("pool",Map.of("active",pool.getActiveConnections(),"idle",pool.getIdleConnections(),
                    "pending",pool.getThreadsAwaitingConnection(),"total",pool.getTotalConnections()));
            }
            var stats=emf.unwrap(SessionFactory.class).getStatistics();
            row.put("hibernate",Map.of("queries",stats.getQueryExecutionCount(),"preparedStatements",stats.getPrepareStatementCount(),
                    "connections",stats.getConnectCount(),"entitiesLoaded",stats.getEntityLoadCount()));
            List<Object> queries=new ArrayList<>();
            for(String query:stats.getQueries()) {
                var q=stats.getQueryStatistics(query);
                // Only a stable identifier; SQL/HQL text and bind values are not exported.
                String id=HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(query.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                queries.add(Map.of("id",id,"count",q.getExecutionCount(),"totalMs",q.getExecutionTotalTime(),
                    "maxMs",q.getExecutionMaxTime(),"rows",q.getExecutionRowCount()));
            }
            row.put("queries",queries);
            Files.writeString(output,json.writeValueAsString(row)+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        } catch(Exception ex) {
            // Do not lose evidence silently or leak credentials through exception details.
            System.err.println("Performance telemetry sample failed: "+ex.getClass().getSimpleName());
        }
    }
    @PreDestroy public void stop() {executor.shutdownNow();}
}
