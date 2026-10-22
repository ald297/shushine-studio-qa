package com.shushinestudio.config;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class DatabasePoolWarmupTest {
    @Test void holdsCapacityUntilAllConnectionsAreReadyThenReturnsThem() throws Exception {
        var source=mock(HikariDataSource.class);var first=mock(Connection.class);var second=mock(Connection.class);
        when(source.getMinimumIdle()).thenReturn(2);when(source.getConnection()).thenReturn(first,second);
        when(first.isValid(5)).thenReturn(true);when(second.isValid(5)).thenReturn(true);
        new DatabasePoolWarmup(source).afterPropertiesSet();
        var order=inOrder(source,first,second);
        order.verify(source).getMinimumIdle();order.verify(source).getConnection();order.verify(first).isValid(5);
        order.verify(source).getMinimumIdle();order.verify(source).getConnection();order.verify(second).isValid(5);
        order.verify(source).getMinimumIdle();order.verify(first).close();order.verify(second).close();
    }
    @Test void failedValidationPreventsStartupAndReturnsBorrowedConnections() throws Exception {
        var source=mock(HikariDataSource.class);var connection=mock(Connection.class);
        when(source.getMinimumIdle()).thenReturn(2);when(source.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(false);
        assertThrows(SQLException.class,()->new DatabasePoolWarmup(source).afterPropertiesSet());verify(connection).close();
    }
}
