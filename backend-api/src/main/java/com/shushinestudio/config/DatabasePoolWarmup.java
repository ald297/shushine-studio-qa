package com.shushinestudio.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Validate the configured idle capacity before Spring opens the HTTP connector. */
@Component
@DependsOn("reservationSchemaGuard")
@ConditionalOnProperty(name="database.pool.warmup.enabled",havingValue="true",matchIfMissing=true)
public class DatabasePoolWarmup implements InitializingBean {
    private final DataSource source;
    public DatabasePoolWarmup(DataSource source) {this.source=source;}
    @Override public void afterPropertiesSet() throws SQLException {
        if (!(source instanceof HikariDataSource hikari)) return;
        List<Connection> held=new ArrayList<>();
        try {
            for(int n=0;n<hikari.getMinimumIdle();n++) {
                Connection connection=hikari.getConnection();held.add(connection);
                if(!connection.isValid(5))throw new SQLException("No se pudo preparar el pool de base de datos");
            }
        } finally {
            SQLException failure=null;
            for(Connection connection:held) {
                try {connection.close();} catch(SQLException ex) {if(failure==null)failure=ex;}
            }
            if(failure!=null)throw failure;
        }
    }
}
