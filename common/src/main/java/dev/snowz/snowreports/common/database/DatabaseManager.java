package dev.snowz.snowreports.common.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dev.snowz.snowreports.common.config.Config;
import dev.snowz.snowreports.common.database.type.DatabaseType;
import lombok.Getter;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;

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

    private final LibraryManager libraryManager;
    private final Map<DatabaseType, Boolean> loadedDrivers;

    private final File dataFolder;

    /**
     * Create a platform-agnostic DatabaseManager
     *
     * @param dataFolder     The data folder for the plugin/application
     * @param libraryManager Platform-specific library manager implementation
     */
    public DatabaseManager(final File dataFolder, final LibraryManager libraryManager) {
        this.dataFolder = dataFolder;
        this.libraryManager = libraryManager;
        this.daoMap = new HashMap<>();
        this.loadedDrivers = new HashMap<>();

        // Add Maven Central repository for dependency resolution
        libraryManager.addMavenCentral();
    }

    /**
     * Connect to database using the configuration options
     */
    public void connect(final Config config) throws SQLException {
        this.databaseType = config.getStorageMethod();

        // Load the required database driver dynamically
        loadDatabaseDriver(databaseType);

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
     * Dynamically load the database driver using Libby
     */
    private void loadDatabaseDriver(final DatabaseType databaseType) throws SQLException {
        // Check if driver is already loaded
        if (loadedDrivers.getOrDefault(databaseType, false)) {
            return;
        }

        try {
            final Library driverLibrary = createDriverLibrary(databaseType);
            if (driverLibrary != null) {
                logger.info("Loading database driver for " + databaseType.name() + "...");
                libraryManager.loadLibrary(driverLibrary);
                loadedDrivers.put(databaseType, true);
                logger.info("Successfully loaded " + databaseType.name() + " driver");
            }
        } catch (final Exception e) {
            throw new SQLException("Failed to load database driver for " + databaseType.name(), e);
        }
    }

    /**
     * Create Library instance for the specified database type
     */
    private Library createDriverLibrary(final DatabaseType databaseType) {
        return switch (databaseType) {
            case MYSQL -> Library.builder()
                .groupId("com{}mysql")
                .artifactId("mysql-connector-j")
                .version("9.3.0")
                .id("mysql-driver")
                .relocate("com{}mysql", "dev{}snowz{}snowreports{}libs{}mysql")
                .build();
            case MARIADB -> Library.builder()
                .groupId("org{}mariadb{}jdbc")
                .artifactId("mariadb-java-client")
                .version("3.5.3")
                .id("mariadb-driver")
                .relocate("org{}mariadb", "dev{}snowz{}snowreports{}libs{}mariadb")
                .build();
            case POSTGRESQL -> Library.builder()
                .groupId("org{}postgresql")
                .artifactId("postgresql")
                .version("42.7.5")
                .id("postgresql-driver")
                .relocate("org{}postgresql", "dev{}snowz{}snowreports{}libs{}postgresql")
                .build();
            case H2 -> Library.builder()
                .groupId("com{}h2database")
                .artifactId("h2")
                .version("2.3.232")
                .id("h2-driver")
                .relocate("org{}h2", "dev{}snowz{}snowreports{}libs{}h2")
                .build();
            case SQLITE -> Library.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.49.1.0")
                .id("sqlite-driver")
                .relocate("org{}sqlite", "dev{}snowz{}snowreports{}libs{}sqlite")
                .build();
        };
    }

    /**
     * Connect to database with connection details
     */
    private void connect(final String connectionUrl, final String username, final String password) throws SQLException {
        try {
            // Verify the driver class is available
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
                    ". Driver may not have loaded correctly.", e
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
     * Get database driver class name (with relocated packages)
     */
    private String getDriverClass(final DatabaseType databaseType) {
        return switch (databaseType) {
            case MYSQL -> "dev.snowz.snowreports.libs.mysql.cj.jdbc.Driver";
            case MARIADB -> "dev.snowz.snowreports.libs.mariadb.jdbc.Driver";
            case POSTGRESQL -> "dev.snowz.snowreports.libs.postgresql.Driver";
            case H2 -> "dev.snowz.snowreports.libs.h2.Driver";
            case SQLITE -> "dev.snowz.snowreports.libs.sqlite.JDBC";
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
     * Preload all database drivers for faster connection switching
     */
    public void preloadAllDrivers() {
        logger.info("Pre-loading all database drivers...");

        for (final DatabaseType type : DatabaseType.values()) {
            try {
                loadDatabaseDriver(type);
            } catch (final SQLException e) {
                logger.warning("Failed to pre-load driver for " + type.name() + ": " + e.getMessage());
            }
        }

        logger.info("Finished pre-loading database drivers");
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
            // Load driver first
            loadDatabaseDriver(config.getStorageMethod());

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
     * Get information about loaded drivers
     */
    public Map<DatabaseType, Boolean> getLoadedDrivers() {
        return new HashMap<>(loadedDrivers);
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
