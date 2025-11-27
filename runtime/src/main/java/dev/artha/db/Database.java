package dev.artha.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Singleton class for managing database connections using HikariCP.
 * Configured via artha.json database section.
 */
public class Database {
    private static Database instance;
    private HikariDataSource dataSource;
    private boolean initialized = false;

    private Database() {
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Initialize the database connection pool from artha.json config
     * 
     * @param config Database configuration map from artha.json
     */
    public void initialize(Map<String, Object> config) {
        if (initialized) {
            return; // Already initialized
        }

        try {
            String driver = (String) config.get("driver");
            String host = (String) config.getOrDefault("host", "localhost");
            Object portObj = config.get("port");
            int port = 0;
            if (portObj != null) {
                port = portObj instanceof Integer ? (Integer) portObj : Integer.parseInt(portObj.toString());
            }
            String dbName = (String) config.get("name");
            String username = (String) config.get("username");
            String password = (String) config.get("password");

            // Build JDBC URL based on driver type
            String jdbcUrl;
            switch (driver.toLowerCase()) {
                case "postgresql":
                case "postgres":
                    jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
                    break;
                case "mysql":
                    jdbcUrl = String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
                    break;
                case "sqlite":
                    jdbcUrl = "jdbc:sqlite:" + dbName;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported database driver: " + driver);
            }

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcUrl);
            if (username != null)
                hikariConfig.setUsername(username);
            if (password != null)
                hikariConfig.setPassword(password);

            // Connection pool settings
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(30000);

            dataSource = new HikariDataSource(hikariConfig);
            initialized = true;

            System.out.println("✅ Database connected: " + jdbcUrl);
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Get a connection from the pool
     */
    public Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new IllegalStateException("Database not initialized! Add database config to artha.json");
        }
        return dataSource.getConnection();
    }

    /**
     * Check if database is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Shutdown the connection pool
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("🔌 Database connection pool closed");
        }
    }
}
