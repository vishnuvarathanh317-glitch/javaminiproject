package com.hostelmanagement.database;

import com.hostelmanagement.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized JDBC connection factory.
 * All database access goes through this utility — connections are never
 * opened directly in JavaFX event handlers.
 *
 * Uses try-with-resources in callers to guarantee cleanup.
 */
public class DatabaseConnection {

    private DatabaseConnection() {} // utility class — no instantiation

    /**
     * Returns a connection to the application database (smart_hostel_db).
     * Callers MUST close this with try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        AppConfig config = AppConfig.getInstance();
        return DriverManager.getConnection(
                config.getDatabaseJdbcUrl(),
                config.getDbUsername(),
                config.getDbPassword());
    }

    /**
     * Returns a server-level connection (no database selected).
     * Used by DatabaseInitializer to CREATE DATABASE.
     */
    public static Connection getServerConnection() throws SQLException {
        AppConfig config = AppConfig.getInstance();
        return DriverManager.getConnection(
                config.getServerJdbcUrl(),
                config.getDbUsername(),
                config.getDbPassword());
    }

    /** Quick connectivity check — returns true if the server is reachable. */
    public static boolean testConnection() {
        try (Connection conn = getServerConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
