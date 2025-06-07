package dev.snowz.snowreports.common.database.type;

/**
 * Enum representing the supported database types for SnowReports
 */
public enum DatabaseType {
    /**
     * MySQL database - requires connection information
     */
    MYSQL,

    /**
     * MariaDB database - requires connection information
     */
    MARIADB,

    /**
     * PostgreSQL database - requires connection information
     */
    POSTGRESQL,

    /**
     * H2 database - flatfile/local database
     */
    H2,

    /**
     * SQLite database - flatfile/local database
     */
    SQLITE;

    /**
     * Check if this database type requires remote connection information
     */
    public boolean isRemote() {
        return this == MYSQL || this == MARIADB || this == POSTGRESQL;
    }

    /**
     * Check if this database type is a local/flatfile database
     */
    public boolean isLocal() {
        return this == H2 || this == SQLITE;
    }

    /**
     * Get the default port for remote database types
     */
    public int getDefaultPort() {
        return switch (this) {
            case MYSQL, MARIADB -> 3306;
            case POSTGRESQL -> 5432;
            default -> -1; // No default port for local databases
        };
    }
}
