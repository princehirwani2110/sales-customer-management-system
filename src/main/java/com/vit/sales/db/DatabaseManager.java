package com.vit.sales.db;

import com.vit.sales.config.DbConfig;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Central JDBC connection factory. Each call returns a connection owned by the caller. */
public final class DatabaseManager {
    private final DbConfig config;

    public DatabaseManager(DbConfig config) { this.config = config; }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.username(), config.password());
    }

    public static DatabaseManager fromLocalConfig() throws IOException {
        return new DatabaseManager(DbConfig.load());
    }
}
