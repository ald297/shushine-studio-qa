package com.shushinestudio.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ReservationSchemaGuardTest {
    @Test void missingDatabaseProtectionPreventsStartup() {
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(),eq(Boolean.class))).thenReturn(false);
        assertThrows(IllegalStateException.class,()->new ReservationSchemaGuard(jdbc).afterPropertiesSet());
    }
    @Test void verifiedProtectionAllowsStartup() {
        JdbcTemplate jdbc=mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(),eq(Boolean.class))).thenReturn(true);
        assertDoesNotThrow(()->new ReservationSchemaGuard(jdbc).afterPropertiesSet());
    }
}
