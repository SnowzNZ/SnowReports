package dev.snowz.snowreports.common.database.migrator;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import dev.snowz.snowreports.common.database.DatabaseManager;
import dev.snowz.snowreports.common.database.type.DatabaseType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

@ApiStatus.Experimental
public final class DatabaseMigrator {

    private final DatabaseManager dbManager;
    private final ConnectionSource connectionSource;
    private final DatabaseType databaseType;
    private final Logger logger = Logger.getLogger("SnowReports-Database-Migrator");

    public DatabaseMigrator(final DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.connectionSource = dbManager.getConnectionSource();
        this.databaseType = dbManager.getDatabaseType();
    }

    /**
     * Migrate entities.
     */
    public void migrateEntities(final Class<?>... entityClasses) throws SQLException {
        for (final Class<?> entity : entityClasses) {
            migrateEntity(entity);
        }
    }

    /**
     * Migrate an entity.
     */
    public void migrateEntity(final Class<?> entityClass) throws SQLException {
        logger.info("Starting migration for entity: " + entityClass.getName());

        try {
            TableUtils.createTableIfNotExists(connectionSource, entityClass);
            logger.info("Created/verified table for entity: " + entityClass.getSimpleName());
        } catch (final SQLException e) {
            if (dbManager.isDuplicateIndexError(e)) {
                logger.info("Table and indexes already exist for entity: " + entityClass.getSimpleName());
            } else {
                throw e;
            }
        }

        final String tableName = resolveTableName(entityClass);
        final Map<String, ColumnDef> expected = extractExpectedColumns(entityClass);
        final Map<String, ColumnDef> actual = readActualColumns(tableName);

        logger.info("Table: " + tableName + " - expected columns: " + expected.keySet());
        logger.info("Table: " + tableName + " - actual columns: " + actual.keySet());

        final Set<String> toAdd = new HashSet<>();
        final Set<String> toDrop = new HashSet<>();
        final Set<ColumnRename> toRename = new HashSet<>();
        final Set<ColumnTypeChange> toChangeType = new HashSet<>();

        for (final String col : expected.keySet()) {
            if (!actual.containsKey(col)) toAdd.add(col);
        }
        for (final String col : actual.keySet()) {
            if (!expected.containsKey(col)) toDrop.add(col);
        }

        final Set<String> unmatchedExpected = new HashSet<>(toAdd);
        final Set<String> unmatchedActual = new HashSet<>(toDrop);
        final Set<String> renamedColumns = new HashSet<>();

        for (final Iterator<String> itExp = unmatchedExpected.iterator(); itExp.hasNext(); ) {
            final String exp = itExp.next();
            final ColumnDef eDef = expected.get(exp);
            String bestMatch = null;
            int bestScore = Integer.MAX_VALUE;
            for (final String act : unmatchedActual) {
                final ColumnDef aDef = actual.get(act);
                if (aDef == null) continue;
                if (!aDef.sqlType.equalsIgnoreCase(eDef.sqlType))
                    continue;
                final int score = nameDistance(exp, act);
                if (score < bestScore) {
                    bestScore = score;
                    bestMatch = act;
                }
            }
            if (bestMatch != null && bestScore <= 4) {
                toRename.add(new ColumnRename(bestMatch, exp, eDef.sqlType));
                unmatchedActual.remove(bestMatch);
                renamedColumns.add(exp);
                itExp.remove();
            }
        }
        toAdd.removeAll(renamedColumns);
        toDrop.removeAll(unmatchedActual);

        for (final String col : expected.keySet()) {
            if (actual.containsKey(col)) {
                final ColumnDef e = expected.get(col);
                final ColumnDef a = actual.get(col);
                if (!normalizeSqlType(e.sqlType).equalsIgnoreCase(normalizeSqlType(a.sqlType))) {
                    if (isColumnEmpty(tableName, col)) {
                        toChangeType.add(new ColumnTypeChange(col, a.sqlType, e.sqlType));
                    } else {
                        logger.warning(String.format(
                            "Type change skipped for %s.%s because column is not empty (would risk data loss).",
                            tableName,
                            col
                        ));
                    }
                }
            }
        }

        for (final String addCol : toAdd) {
            final ColumnDef def = expected.get(addCol);
            final String addSql = buildAddColumnSql(tableName, addCol, def.sqlType, def.nullable, def.defaultValue);
            logger.info("Adding column: " + addCol + " SQL: " + addSql);
            executeDDL(addSql);
        }

        for (final ColumnRename rn : toRename) {
            logger.info(String.format("Renaming column %s -> %s (type=%s)", rn.oldName, rn.newName, rn.sqlType));
            final boolean done = renameColumn(tableName, rn.oldName, rn.newName, rn.sqlType);
            if (!done) {
                logger.info("Rename via ALTER not supported on this DB. Recreating table to apply rename.");
                recreateTableWithChanges(tableName, expected, actual, Map.of(rn.oldName, rn.newName));
            } else {
                logger.info("Rename executed for " + rn.oldName);
            }
        }

        for (final ColumnTypeChange ctc : toChangeType) {
            logger.info(String.format(
                "Changing type for %s.%s from %s to %s",
                tableName,
                ctc.column,
                ctc.oldType,
                ctc.newType
            ));
            final boolean success = changeColumnType(tableName, ctc.column, ctc.newType);
            if (!success) {
                logger.info("Type change not supported by vendor via direct ALTER. Recreating table to change type.");
                recreateTableWithChanges(tableName, expected, actual, Collections.emptyMap());
            } else {
                logger.info("Type changed for " + ctc.column);
            }
        }

        for (final String dropCol : toDrop) {
            logger.info("Dropping column: " + dropCol);
            final boolean success = dropColumn(tableName, dropCol);
            if (!success) {
                logger.info("Drop column not supported. Recreating table to drop column.");
                recreateTableWithChanges(tableName, expected, actual, Collections.emptyMap());
                break;
            } else {
                logger.info("Dropped column: " + dropCol);
            }
        }

        logger.info("Migration finished for entity: " + entityClass.getName());
    }

    private String resolveTableName(final Class<?> entityClass) {
        final DatabaseTable dt = entityClass.getAnnotation(DatabaseTable.class);
        if (dt != null && dt.tableName() != null && !dt.tableName().isEmpty()) return dt.tableName();
        return entityClass.getSimpleName();
    }

    private Map<String, ColumnDef> extractExpectedColumns(final Class<?> entityClass) {
        final Map<String, ColumnDef> out = new LinkedHashMap<>();
        for (final Field f : getAllFields(entityClass)) {
            final DatabaseField df = f.getAnnotation(DatabaseField.class);
            if (df == null) continue;
            String col = df.columnName();
            if (col == null || col.trim().isEmpty()) col = f.getName();
            final String sqlType = mapJavaTypeToSql(f.getType());
            final boolean nullable = df.canBeNull();
            out.put(col, new ColumnDef(col, sqlType, nullable, null));
        }
        return out;
    }

    private List<Field> getAllFields(final Class<?> cls) {
        final List<Field> fields = new ArrayList<>();
        Class<?> cur = cls;
        while (cur != null && cur != Object.class) {
            fields.addAll(Arrays.asList(cur.getDeclaredFields()));
            cur = cur.getSuperclass();
        }
        return fields;
    }

    private Map<String, ColumnDef> readActualColumns(final String tableName) throws SQLException {
        final Map<String, ColumnDef> out = new LinkedHashMap<>();
        DatabaseConnection dbConn = null;
        try {
            dbConn = connectionSource.getReadOnlyConnection(tableName);
            final Connection conn = dbConn.getUnderlyingConnection();
            final DatabaseMetaData md = conn.getMetaData();
            final String schema = conn.getSchema();
            final ResultSet rs = md.getColumns(conn.getCatalog(), schema, tableName, null);
            while (rs.next()) {
                final String name = rs.getString("COLUMN_NAME");
                final String type = rs.getString("TYPE_NAME");
                final int nullable = rs.getInt("NULLABLE");
                out.put(name, new ColumnDef(name, type, nullable != DatabaseMetaData.columnNoNulls, null));
            }
            rs.close();
        } catch (final SQLException e) {
            logger.warning("JDBC metadata read failed for " + tableName + ": " + e.getMessage());
            if (databaseType == DatabaseType.SQLITE) {
                try {
                    dbConn = connectionSource.getReadOnlyConnection(tableName);
                    final Connection conn = dbConn.getUnderlyingConnection();
                    final Statement st = conn.createStatement();
                    final ResultSet rs = st.executeQuery("PRAGMA table_info('" + tableName + "')");
                    while (rs.next()) {
                        final String name = rs.getString("name");
                        final String type = rs.getString("type");
                        final boolean nullable = rs.getInt("notnull") == 0;
                        out.put(name, new ColumnDef(name, type, nullable, null));
                    }
                    rs.close();
                    st.close();
                } catch (final SQLException ex) {
                    logger.severe("Failed to read table info for SQLite: " + ex.getMessage());
                }
            } else {
                throw e;
            }
        } finally {
            if (dbConn != null) {
                connectionSource.releaseConnection(dbConn);
            }
        }
        return out;
    }

    private boolean isColumnEmpty(final String tableName, final String column) throws SQLException {
        DatabaseConnection dbConn = null;
        try {
            dbConn = connectionSource.getReadOnlyConnection(tableName);
            final Connection conn = dbConn.getUnderlyingConnection();
            final String q = "SELECT COUNT(*) FROM " + quoteIdentifier(tableName) + " WHERE " + quoteIdentifier(column) + " IS NOT NULL";
            try (final Statement st = conn.createStatement(); final ResultSet rs = st.executeQuery(q)) {
                if (rs.next()) {
                    final long count = rs.getLong(1);
                    return count == 0;
                }
            }
        } catch (final SQLException e) {
            logger.warning("Failed to check if column is empty: " + e.getMessage());
            return false;
        } finally {
            if (dbConn != null) {
                connectionSource.releaseConnection(dbConn);
            }
        }
        return false;
    }

    private String buildAddColumnSql(
        final String tableName,
        final String columnName,
        final String sqlType,
        final boolean nullable,
        final String defaultValue
    ) {
        return String.format(
            "ALTER TABLE %s ADD COLUMN %s %s %s %s",
            quoteIdentifier(tableName),
            quoteIdentifier(columnName),
            sqlType,
            (nullable ? "" : "NOT NULL"),
            (defaultValue != null ? "DEFAULT " + defaultValue : "")
        ).trim();
    }

    private boolean renameColumn(
        final String tableName,
        final String oldName,
        final String newName,
        final String sqlType
    ) {
        final String sql;
        try {
            switch (databaseType) {
                case POSTGRESQL, H2:
                    sql = String.format(
                        "ALTER TABLE %s RENAME COLUMN %s TO %s",
                        quoteIdentifier(tableName), quoteIdentifier(oldName), quoteIdentifier(newName)
                    );
                    executeDDL(sql);
                    return true;
                case MYSQL:
                case MARIADB:
                    sql = String.format(
                        "ALTER TABLE %s CHANGE %s %s %s",
                        quoteIdentifier(tableName),
                        quoteIdentifier(oldName),
                        quoteIdentifier(newName),
                        sqlType
                    );
                    executeDDL(sql);
                    return true;
                case SQLITE:
                default:
                    return false;
            }
        } catch (final SQLException e) {
            logger.warning("Rename failed: " + e.getMessage());
            return false;
        }
    }

    private boolean changeColumnType(final String tableName, final String column, final String newType) {
        try {
            return switch (databaseType) {
                case POSTGRESQL -> {
                    executeDDL(String.format(
                        "ALTER TABLE %s ALTER COLUMN %s TYPE %s",
                        quoteIdentifier(tableName), quoteIdentifier(column), newType
                    ));
                    yield true;
                }
                case MYSQL, MARIADB -> {
                    executeDDL(String.format(
                        "ALTER TABLE %s MODIFY COLUMN %s %s",
                        quoteIdentifier(tableName), quoteIdentifier(column), newType
                    ));
                    yield true;
                }
                case H2 -> {
                    executeDDL(String.format(
                        "ALTER TABLE %s ALTER COLUMN %s %s",
                        quoteIdentifier(tableName), quoteIdentifier(column), newType
                    ));
                    yield true;
                }
                default -> false;
            };
        } catch (final SQLException e) {
            logger.warning("Change type failed: " + e.getMessage());
            return false;
        }
    }

    private boolean dropColumn(final String tableName, final String column) {
        try {
            return switch (databaseType) {
                case POSTGRESQL, MYSQL, MARIADB, H2 -> {
                    executeDDL(String.format(
                        "ALTER TABLE %s DROP COLUMN %s",
                        quoteIdentifier(tableName),
                        quoteIdentifier(column)
                    ));
                    yield true;
                }
                default -> false;
            };
        } catch (final SQLException e) {
            logger.warning("Drop column failed: " + e.getMessage());
            return false;
        }
    }

    private void recreateTableWithChanges(
        final String tableName,
        final Map<String, ColumnDef> expected,
        final Map<String, ColumnDef> actual,
        final Map<String, String> renameMap
    ) throws SQLException {
        final String tmpTable = tableName + "_tmp_mig";
        logger.info("Recreating table " + tableName + " as " + tmpTable);

        final StringBuilder create = new StringBuilder("CREATE TABLE ").append(quoteIdentifier(tmpTable)).append(" (");
        boolean first = true;
        for (final ColumnDef def : expected.values()) {
            if (!first) create.append(", ");
            first = false;
            create.append(quoteIdentifier(def.name)).append(" ").append(def.sqlType);
            if (!def.nullable) create.append(" NOT NULL");
        }
        create.append(")");
        executeDDL(create.toString());

        final List<String> actualColsForSelect = new ArrayList<>();
        final List<String> tmpColsForInsert = new ArrayList<>();
        for (final String exp : expected.keySet()) {
            String actualName = exp;
            final Optional<String> maybeOld = renameMap.entrySet().stream()
                .filter(e -> e.getValue().equals(exp))
                .map(Map.Entry::getKey)
                .findFirst();
            if (maybeOld.isPresent() && actual.containsKey(maybeOld.get())) {
                actualName = maybeOld.get();
            }
            if (actual.containsKey(actualName)) {
                actualColsForSelect.add(quoteIdentifier(actualName));
                tmpColsForInsert.add(quoteIdentifier(exp));
            }
        }

        final String selectList = String.join(", ", actualColsForSelect);
        final String insertList = String.join(", ", tmpColsForInsert);
        final String copySql = String.format(
            "INSERT INTO %s (%s) SELECT %s FROM %s",
            quoteIdentifier(tmpTable), insertList, selectList, quoteIdentifier(tableName)
        );
        executeDDL(copySql);

        executeDDL("DROP TABLE " + quoteIdentifier(tableName));

        switch (databaseType) {
            case SQLITE:
            case POSTGRESQL:
            case H2:
            case MYSQL:
            case MARIADB:
            default:
                executeDDL(String.format(
                    "ALTER TABLE %s RENAME TO %s",
                    quoteIdentifier(tmpTable),
                    quoteIdentifier(tableName)
                ));
                break;
        }
        logger.info("Recreated table " + tableName + " successfully.");
    }

    private void executeDDL(final String sql) throws SQLException {
        DatabaseConnection dbConn = null;
        try {
            dbConn = connectionSource.getReadWriteConnection(null);
            final Connection conn = dbConn.getUnderlyingConnection();
            try (final Statement st = conn.createStatement()) {
                logger.info("Executing DDL: " + sql);
                st.execute(sql);
            }
        } finally {
            if (dbConn != null) {
                connectionSource.releaseConnection(dbConn);
            }
        }
    }

    private record ColumnDef(String name, String sqlType, boolean nullable, String defaultValue) {
    }

    private record ColumnRename(String oldName, String newName, String sqlType) {
    }

    private record ColumnTypeChange(String column, String oldType, String newType) {
    }

    private String mapJavaTypeToSql(final Class<?> cls) {
        if (cls == String.class) return switch (databaseType) {
            case MYSQL, MARIADB, H2 -> "VARCHAR(255)";
            default -> "TEXT";
        };
        if (cls == int.class || cls == Integer.class) return "INTEGER";
        if (cls == long.class || cls == Long.class) return "BIGINT";
        if (cls == boolean.class || cls == Boolean.class) {
            return databaseType == DatabaseType.SQLITE ? "INTEGER" : "BOOLEAN";
        }
        if (cls == float.class || cls == Float.class) return "REAL";
        if (cls == double.class || cls == Double.class) return "DOUBLE";
        if (cls == java.util.Date.class || cls == java.sql.Timestamp.class) return "TIMESTAMP";

        return "TEXT";
    }

    private String normalizeSqlType(final String t) {
        if (t == null) return "";

        final String normalized = t.replaceAll("\\(.*\\)", "").trim().toUpperCase();

        return switch (normalized) {
            case "INT" -> "INTEGER";
            case "BOOL" -> "BOOLEAN";
            case "CHAR" -> "CHARACTER";
            case "VARCHAR" -> "CHARACTER VARYING";
            case "TIMESTAMP", "DATETIME" -> "TIMESTAMP";
            default -> normalized;
        };
    }

    private String quoteIdentifier(final String id) {
        if (id == null) return null;
        return switch (databaseType) {
            case MYSQL, MARIADB -> "`" + id + "`";
            default -> "\"" + id + "\"";
        };
    }

    private static int nameDistance(String a, String b) {
        a = a == null ? "" : a.toLowerCase();
        b = b == null ? "" : b.toLowerCase();
        final int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) d[i][0] = i;
        for (int j = 0; j <= b.length(); j++) d[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                final int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
            }
        }
        return d[a.length()][b.length()];
    }
}
