package dev.snowz.snowreports.common.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.type.DatabaseType;
import lombok.Getter;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class DatabaseManager {

    private static final Logger logger = Logger.getLogger("SnowRepots-Database");

    @Getter
    private ConnectionSource connectionSource;

    @Getter
    private DatabaseType databaseType;
    private final Map<Class<?>, Dao<?, ?>> daoMap;
    private boolean isConnected = false;

    private final File dataFolder;

    /**
     * Create a platform-agnostic DatabaseManager
     *
     * @param dataFolder The data folder for the plugin/application
     */
    public DatabaseManager(final File dataFolder) {
        this.dataFolder = dataFolder;
        this.daoMap = new HashMap<>();
    }

    /**
     * Connect to database using the configuration options
     */
    public void connect(final Config config) throws SQLException {
        this.databaseType = config.getStorageMethod();

        final String connectionUrl = buildConnectionUrl(config);
        String username = null;
        String password = null;

        // Only set credentials for remote databases
        if (requiresCredentials(databaseType)) {
            username = config.getDatabase().getUsername();
            password = config.getDatabase().getPassword();
        }

        connect(connectionUrl, username, password);
    }

    /**
     * Connect to database with connection details
     */
    private void connect(final String connectionUrl, final String username, final String password) throws SQLException {
        try {
            // Verify the driver class is available (provided by the platform loader)
            Class.forName(getDriverClass(databaseType));

            // Create connection source
            if (username != null && password != null) {
                connectionSource = new JdbcConnectionSource(connectionUrl, username, password);
            } else {
                connectionSource = new JdbcConnectionSource(connectionUrl);
            }

            isConnected = true;
            logger.info("Successfully connected to " + databaseType.name() + " database");

        } catch (final ClassNotFoundException e) {
            throw new SQLException(
                "Database driver not found for: " + databaseType.name() +
                    ". Ensure the driver is provided by the platform loader.", e
            );
        }
    }

    /**
     * Build connection URL based on database type and configuration
     */
    private String buildConnectionUrl(final Config config) {
        final Config.DatabaseConfig dbConfig = config.getDatabase();
        final StringBuilder url = new StringBuilder();

        switch (databaseType) {
            case SQLITE:
                url.append("jdbc:sqlite:")
                    .append(dataFolder)
                    .append(File.separator)
                    .append("snowreports.sqlite");
                break;

            // FIXME
            case H2:
                url.append("jdbc:h2:file:")
                    .append(dataFolder)
                    .append(File.separator)
                    .append("snowreports.h2");
                break;

            case MYSQL:
                url.append("jdbc:mysql://")
                    .append(dbConfig.getHost()).append(":").append(dbConfig.getPort())
                    .append("/").append(dbConfig.getDatabase());

                // Add MySQL-specific parameters
                url.append("?useSSL=").append(dbConfig.getAdvanced().isUseSsl())
                    .append("&verifyServerCertificate=").append(dbConfig.getAdvanced().isVerifyServerCertificate())
                    .append("&allowPublicKeyRetrieval=").append(dbConfig.getAdvanced().isAllowPublicKeyRetrieval())
                    .append("&serverTimezone=UTC");
                break;

            case MARIADB:
                url.append("jdbc:mariadb://")
                    .append(dbConfig.getHost()).append(":").append(dbConfig.getPort())
                    .append("/").append(dbConfig.getDatabase());

                // Add MariaDB-specific parameters
                url.append("?useSSL=").append(dbConfig.getAdvanced().isUseSsl())
                    .append("&verifyServerCertificate=").append(dbConfig.getAdvanced().isVerifyServerCertificate())
                    .append("&allowPublicKeyRetrieval=").append(dbConfig.getAdvanced().isAllowPublicKeyRetrieval());
                break;

            case POSTGRESQL:
                url.append("jdbc:postgresql://")
                    .append(dbConfig.getHost()).append(":").append(dbConfig.getPort())
                    .append("/").append(dbConfig.getDatabase());

                // Add PostgreSQL-specific parameters
                if (dbConfig.getAdvanced().isUseSsl()) {
                    url.append("?ssl=true");
                    if (!dbConfig.getAdvanced().isVerifyServerCertificate()) {
                        url.append("&sslmode=require");
                    }
                } else {
                    url.append("?ssl=false");
                }
                break;
        }

        return url.toString();
    }

    /**
     * Get database driver class name
     */
    private String getDriverClass(final DatabaseType databaseType) {
        return switch (databaseType) {
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case MARIADB -> "org.mariadb.jdbc.Driver";
            case POSTGRESQL -> "org.postgresql.Driver";
            case H2 -> "org.h2.Driver";
            case SQLITE -> "org.sqlite.JDBC";
        };
    }

    /**
     * Check if database type requires credentials
     */
    private boolean requiresCredentials(final DatabaseType databaseType) {
        return databaseType == DatabaseType.MYSQL ||
            databaseType == DatabaseType.MARIADB ||
            databaseType == DatabaseType.POSTGRESQL;
    }

    /**
     * Create tables for the given entity classes with table prefix
     */
    // TODO: Implement table prefix handling
    public void createTables(final Config config, final Class<?>... entityClasses) throws SQLException {
        createTables(entityClasses);
    }

    /**
     * Create tables for the given entity classes
     */
    public void createTables(final Class<?>... entityClasses) throws SQLException {
        checkConnection();

        for (final Class<?> entityClass : entityClasses) {
            try {
                TableUtils.createTableIfNotExists(connectionSource, entityClass);
                logger.info("Created/verified table for entity: " + entityClass.getSimpleName());
            } catch (final SQLException e) {
                if (isDuplicateIndexError(e)) {
                    logger.info("Table and indexes already exist for entity: " + entityClass.getSimpleName());
                } else {
                    throw e;
                }
            } catch (final RuntimeException e) {
                if (e.getCause() instanceof final SQLException sqlException) {
                    if (isDuplicateIndexError(sqlException)) {
                        logger.info("Table and indexes already exist for entity: " + entityClass.getSimpleName());
                    } else {
                        throw sqlException;
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Drop tables for the given entity classes
     */
    public void dropTables(final Class<?>... entityClasses) throws SQLException {
        checkConnection();

        for (final Class<?> entityClass : entityClasses) {
            TableUtils.dropTable(connectionSource, entityClass, true);
            logger.info("Dropped table for entity: " + entityClass.getSimpleName());
        }
    }

    /**
     * Get DAO for a specific entity class
     */
    @SuppressWarnings("unchecked")
    public <T, ID> Dao<T, ID> getDao(final Class<T> entityClass) throws SQLException {
        checkConnection();

        if (!daoMap.containsKey(entityClass)) {
            final Dao<T, ID> dao = DaoManager.createDao(connectionSource, entityClass);
            daoMap.put(entityClass, dao);
        }

        return (Dao<T, ID>) daoMap.get(entityClass);
    }

    /**
     * Check if connected to database
     */
    public boolean isConnected() {
        return isConnected && connectionSource != null;
    }

    /**
     * Test database connection
     */
    public boolean testConnection(final Config config) {
        try {
            // Test connection
            connect(config);
            final boolean connected = isConnected();
            close();
            return connected;
        } catch (final SQLException e) {
            logger.warning("Database connection test failed: " + e.getMessage());
            return false;
        }
    }


    /**
     * Close database connection
     */
    public void close() {
        if (connectionSource != null) {
            try {
                connectionSource.close();
                isConnected = false;
                daoMap.clear();
                logger.info("Database connection closed");
            } catch (final Exception e) {
                logger.severe("Error closing database connection: " + e.getMessage());
            }
        }
    }

    private void checkConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Not connected to database. Call connect() first.");
        }
    }

    /**
     * Check if the SQLException is related to duplicate indexes
     */
    public boolean isDuplicateIndexError(final SQLException e) {
        final String message = e.getMessage().toLowerCase();

        // Check the main exception
        boolean isDuplicate = message.contains("duplicate key name") ||
            message.contains("duplicate index") ||
            message.contains("index already exists") ||
            message.contains("already exists");

        // Also check the cause chain
        Throwable cause = e.getCause();
        while (cause != null && !isDuplicate) {
            final String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                final String lowerCauseMessage = causeMessage.toLowerCase();
                isDuplicate = lowerCauseMessage.contains("duplicate key name") ||
                    lowerCauseMessage.contains("duplicate index") ||
                    lowerCauseMessage.contains("index already exists") ||
                    lowerCauseMessage.contains("already exists");
            }
            cause = cause.getCause();
        }

        return isDuplicate;
    }
}
