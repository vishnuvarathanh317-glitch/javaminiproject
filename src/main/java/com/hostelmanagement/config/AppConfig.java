package com.hostelmanagement.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads database configuration from application.properties.
 * Never exposes the password in toString() or logs.
 */
public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;

    private final String dbHost;
    private final int dbPort;
    private final String dbName;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbTimezone;
    private final boolean dbUseSSL;

    private AppConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new RuntimeException(
                    "Configuration file '" + CONFIG_FILE + "' not found in resources.\n"
                    + "Copy application.properties.example to application.properties "
                    + "and set your MySQL credentials.");
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration: " + e.getMessage(), e);
        }

        this.dbHost = props.getProperty("db.host", "localhost");
        this.dbPort = Integer.parseInt(props.getProperty("db.port", "3306"));
        this.dbName = props.getProperty("db.name", "smart_hostel_db");
        this.dbUsername = props.getProperty("db.username", "root");
        this.dbPassword = props.getProperty("db.password", "");
        this.dbTimezone = props.getProperty("db.serverTimezone", "Asia/Kolkata");
        this.dbUseSSL = Boolean.parseBoolean(props.getProperty("db.useSSL", "false"));
    }

    /** Thread-safe singleton. */
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /** JDBC URL without database name (for bootstrap/create-database connection). */
    public String getServerJdbcUrl() {
        return String.format("jdbc:mysql://%s:%d?useSSL=%b&serverTimezone=%s&allowPublicKeyRetrieval=true",
                dbHost, dbPort, dbUseSSL, dbTimezone);
    }

    /** JDBC URL with database name (for normal application use). */
    public String getDatabaseJdbcUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=%b&serverTimezone=%s&allowPublicKeyRetrieval=true",
                dbHost, dbPort, dbName, dbUseSSL, dbTimezone);
    }

    public String getDbName()     { return dbName; }
    public String getDbUsername() { return dbUsername; }
    public String getDbPassword() { return dbPassword; }

    @Override
    public String toString() {
        return "AppConfig{host=" + dbHost + ", port=" + dbPort
             + ", db=" + dbName + ", user=" + dbUsername + "}";
    }
}
