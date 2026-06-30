package com.drdo.gis.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    public static synchronized void init() {
        if (dataSource != null) return;

        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                log.error("db.properties not found on classpath");
                throw new RuntimeException("db.properties not found");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getProperty("db.url", "jdbc:postgresql://localhost:5432/defence_gis"));
        cfg.setUsername(props.getProperty("db.username", "postgres"));
        cfg.setPassword(props.getProperty("db.password", ""));
        cfg.setDriverClassName(props.getProperty("db.driver", "org.postgresql.Driver"));
        cfg.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
        cfg.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "2")));
        cfg.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        cfg.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
        cfg.setPoolName("DefenceGIS-Pool");

        dataSource = new HikariDataSource(cfg);
        log.info("HikariCP pool initialized: {}", cfg.getJdbcUrl());
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            init();
        }
        return dataSource.getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("HikariCP pool shut down");
        }
    }
}
