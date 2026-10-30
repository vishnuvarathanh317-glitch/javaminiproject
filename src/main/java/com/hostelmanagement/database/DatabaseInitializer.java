package com.hostelmanagement.database;

import com.hostelmanagement.config.AppConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * Automatic database initializer that:
 * 1. Creates the smart_hostel_db database if it doesn't exist.
 * 2. Runs versioned SQL migrations idempotently.
 * 3. Never drops tables or deletes existing data.
 * 4. Reports clear errors with corrective instructions.
 */
public class DatabaseInitializer {

    private static final String[] MIGRATIONS = {
        "V1__initial_schema.sql"
    };

    /**
     * Call once at application startup.
     * Creates the database and applies pending migrations.
     */
    public void initialize() {
        System.out.println("[DB-INIT] Starting database initialization...");

        createDatabaseIfNotExists();
        applyMigrations();

        System.out.println("[DB-INIT] Database initialization complete.");
    }

    /** Step 1: Connect to MySQL server and CREATE DATABASE IF NOT EXISTS. */
    private void createDatabaseIfNotExists() {
        AppConfig config = AppConfig.getInstance();
        String dbName = config.getDbName();

        try (Connection conn = DatabaseConnection.getServerConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName
                    + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("[DB-INIT] Database '" + dbName + "' ready.");

        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("Access denied")) {
                throw new RuntimeException(
                    "[DB-INIT] ERROR: Access denied for user '" + config.getDbUsername() + "'.\n"
                    + "The configured account lacks CREATE DATABASE privileges.\n"
                    + "Fix: Ask your MySQL administrator to run:\n"
                    + "  GRANT ALL PRIVILEGES ON `" + dbName + "`.* TO '"
                    + config.getDbUsername() + "'@'localhost';\n"
                    + "  FLUSH PRIVILEGES;\n"
                    + "Or update application.properties with a privileged account.", e);
            }
            if (msg.contains("Communications link failure") || msg.contains("Connection refused")) {
                throw new RuntimeException(
                    "[DB-INIT] ERROR: Cannot connect to MySQL server at "
                    + config.getDbName() + ".\n"
                    + "Is the MySQL service running? Check with:\n"
                    + "  Get-Service -Name '*mysql*'\n"
                    + "Start it with:\n"
                    + "  Start-Service MySQL267", e);
            }
            throw new RuntimeException("[DB-INIT] Failed to create database: " + msg, e);
        }
    }

    /** Step 2: Apply each migration whose version is not yet recorded. */
    private void applyMigrations() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure schema_version table exists (it's in V1 but let's be safe)
            ensureSchemaVersionTable(conn);

            for (String migrationFile : MIGRATIONS) {
                int version = extractVersion(migrationFile);
                if (!isMigrationApplied(conn, version)) {
                    System.out.println("[DB-INIT] Applying migration: " + migrationFile);
                    String sql = loadMigrationSQL(migrationFile);
                    executeMigration(conn, sql);
                    recordMigration(conn, version, migrationFile);
                    System.out.println("[DB-INIT] Migration " + migrationFile + " applied successfully.");
                } else {
                    System.out.println("[DB-INIT] Migration " + migrationFile + " already applied, skipping.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("[DB-INIT] Migration failed: " + e.getMessage(), e);
        }
    }

    private void ensureSchemaVersionTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS schema_version ("
                + "version INT PRIMARY KEY, "
                + "description VARCHAR(255) NOT NULL, "
                + "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB");
        }
    }

    private boolean isMigrationApplied(Connection conn, int version) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM schema_version WHERE version = ?")) {
            ps.setInt(1, version);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void recordMigration(Connection conn, int version, String desc) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO schema_version (version, description) VALUES (?, ?)")) {
            ps.setInt(1, version);
            ps.setString(2, desc);
            ps.executeUpdate();
        }
    }

    private int extractVersion(String filename) {
        // "V1__initial_schema.sql" → 1
        String num = filename.substring(1, filename.indexOf("__"));
        return Integer.parseInt(num);
    }

    private String loadMigrationSQL(String filename) {
        String path = "db/migration/" + filename;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Migration file not found: " + path);
            }
            return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read migration file: " + path, e);
        }
    }

    /**
     * Executes a multi-statement SQL migration.
     * Strips SQL comments and executes each statement sequentially.
     */
    private void executeMigration(Connection conn, String sql) throws SQLException {
        // 1. Remove all SQL comment lines (-- ...)
        StringBuilder noComments = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("--")) {
                noComments.append(line).append("\n");
            }
        }

        // 2. Split on semicolons and execute each statement
        String[] statements = noComments.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String s : statements) {
                String finalSql = s.trim();
                if (!finalSql.isEmpty()) {
                    try {
                        stmt.executeUpdate(finalSql);
                    } catch (SQLException e) {
                        // Ignore "duplicate key" and "already exists" for idempotency
                        if (e.getErrorCode() == 1061 || e.getErrorCode() == 1022
                            || e.getMessage().contains("Duplicate")
                            || e.getMessage().contains("already exists")) {
                            System.out.println("[DB-INIT] Skipping (already exists): "
                                + finalSql.substring(0, Math.min(80, finalSql.length())));
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }
    }
}
