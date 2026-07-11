package xyz.srnyx.annoyingapi.storage.dialects;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.byteflux.libby.classloader.IsolatedClassLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.StringData;
import xyz.srnyx.annoyingapi.library.AnnoyingAPILibrary;
import xyz.srnyx.annoyingapi.storage.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * SQL dialect for a specific type of database
 */
public class SQLDialect extends Dialect {
    @NotNull public final HikariDataSource dataSource;
    @NotNull public final DSLContext dsl;
    /**
     * <ul>
     *     <li>Key: table name
     *     <li>Value:<ul>
     *         <li>Key: target
     *         <li>Value:<ul>
     *             <li>Key: data key
     *             <li>Value: data value
     *        </ul>
     *     </ul>
     * </ul>
     */
    @NotNull public final ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>>> cache = new ConcurrentHashMap<>();

    /**
     * Construct a new {@link SQLDialect} with the given {@link DataManager}
     *
     * @param   dataManager         {@link #dataManager}
     *
     * @throws  ConnectionException if a database connection error occurs
     */
    public SQLDialect(@NotNull DataManager dataManager, @NotNull org.jooq.SQLDialect jooqDialect) throws ConnectionException {
        super(dataManager);
        if (dataManager.storageConfig.method.driver == null || dataManager.storageConfig.method.url == null) {
            throw new IllegalStateException("The storage method " + dataManager.storageConfig.method + " is not an SQL method");
        }
        final Path dataPath = dataManager.storageConfig.plugin.getDataFolder().toPath();

        // Get url & properties
        String url = dataManager.storageConfig.method.url.apply(dataPath);
        final Properties properties = new Properties();
        if (dataManager.storageConfig.method.isRemote()) {
            url += dataManager.storageConfig.remote_connection.host + ":" + dataManager.storageConfig.remote_connection.port + "/" + dataManager.storageConfig.remote_connection.database;
            properties.putAll(dataManager.storageConfig.remote_connection.properties);
            if (!dataManager.storageConfig.remote_connection.username.isEmpty()) properties.setProperty("user", dataManager.storageConfig.remote_connection.username);
            if (!dataManager.storageConfig.remote_connection.password.isEmpty()) properties.setProperty("password", dataManager.storageConfig.remote_connection.password);
        }
        final String finalUrl = url;

        // Download HikariCP and jOOQ libraries
        if (dataManager.plugin.libraryManager != null) {
            // HikariCP
            if (!dataManager.plugin.libraryManager.loadLibrary(AnnoyingAPILibrary.HIKARICP)) {
                throw new ConnectionException("Failed to download HikariCP library for " + dataManager.storageConfig.method, finalUrl, properties);
            }
            // jOOQ
            if (!dataManager.plugin.libraryManager.loadLibrary(AnnoyingAPILibrary.JOOQ)) {
                throw new ConnectionException("Failed to download jOOQ library for " + dataManager.storageConfig.method, finalUrl, properties);
            }
        }

        // SQLite: create parent directories
        if (dataManager.storageConfig.method == StorageMethod.SQLITE) {
            final File folder = dataPath.resolve("data").resolve("sqlite").toFile();
            if (!folder.exists() && !folder.mkdirs()) throw new ConnectionException("Failed to create SQLite parent directories", finalUrl, properties);
        }

        // Create HikariConfig
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDataSourceProperties(properties);

        // If downloading library, connect using an IsolatedClassLoader
        if (dataManager.storageConfig.method.library != null && dataManager.plugin.libraryManager != null) {
            // Get IsolatedClassLoader of library
            final IsolatedClassLoader classLoader;
            try {
                classLoader = dataManager.plugin.libraryManager.loadLibraryIsolated(dataManager.storageConfig.method.library);
            } catch (final Exception e) {
                throw new ConnectionException(e, finalUrl, properties);
            }
            if (classLoader == null) throw new ConnectionException("Failed to load library for " + dataManager.storageConfig.method, finalUrl, properties);

            // Get driver class
            final Class<?> driverClass;
            try {
                driverClass = classLoader.loadClass(dataManager.storageConfig.method.driver);
            } catch (final ClassNotFoundException e) {
                throw new ConnectionException(e, finalUrl, properties);
            }

            // Create DSL from IsolatedClassLoader
            hikariConfig.setDataSource(new DataSource() {
                @Override
                public Connection getConnection() throws SQLException {
                    try {
                        return (Connection) driverClass.getMethod("connect", String.class, Properties.class).invoke(driverClass.getDeclaredConstructor().newInstance(), finalUrl, properties);
                    } catch (final Exception e) {
                        throw new SQLException(e);
                    }
                }

                @Override
                public Connection getConnection(String username, String password) {
                    throw new UnsupportedOperationException("getConnection(String, String) is not supported for IsolatedClassLoader");
                }
                @Override
                public PrintWriter getLogWriter() {
                    throw new UnsupportedOperationException("getLogWriter() is not supported for IsolatedClassLoader");
                }
                @Override
                public void setLogWriter(PrintWriter out) {
                    throw new UnsupportedOperationException("setLogWriter(PrintWriter) is not supported for IsolatedClassLoader");
                }
                @Override
                public void setLoginTimeout(int seconds) {
                    throw new UnsupportedOperationException("setLoginTimeout(int) is not supported for IsolatedClassLoader");
                }
                @Override
                public int getLoginTimeout() {
                    throw new UnsupportedOperationException("getLoginTimeout() is not supported for IsolatedClassLoader");
                }
                @Override
                public <T> T unwrap(Class<T> iface) {
                    throw new UnsupportedOperationException("unwrap(Class<T>) is not supported for IsolatedClassLoader");
                }
                @Override
                public boolean isWrapperFor(Class<?> iface) {
                    throw new UnsupportedOperationException("isWrapperFor(Class<?>) is not supported for IsolatedClassLoader");
                }
                @Override
                public Logger getParentLogger() {
                    throw new UnsupportedOperationException("getParentLogger() is not supported for IsolatedClassLoader");
                }
            });
        } else {
            // Driver class already exists on classpath
            hikariConfig.setDriverClassName(dataManager.storageConfig.method.driver);
            hikariConfig.setJdbcUrl(finalUrl);
        }

        // Create DataSource
        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (final Exception e) {
            throw new ConnectionException(e, finalUrl, properties);
        }

        // Create DSL
        dsl = DSL.using(dataSource, jooqDialect);
    }

    @Override @Nullable
    public CachedValue getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        final ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>> tableMap = cache.get(table);
        if (tableMap == null) return null;
        final Map<String, CachedValue> targetMap = tableMap.get(target);
        return targetMap == null ? null : targetMap.get(key);
    }

    @Override
    public void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull CachedValue value) {
        cache.computeIfAbsent(table, k -> new ConcurrentHashMap<>()).computeIfAbsent(target, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    @Override
    public void markRemovedInCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        cache.computeIfAbsent(table, k -> new ConcurrentHashMap<>()).computeIfAbsent(target, k -> new ConcurrentHashMap<>()).put(key, new CachedValue());
    }

    @Override
    public void saveCacheImpl() {
        for (final FailedSet failure : setToDatabase(cache)) dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to save cached &4" + failure.column() + "&c for &4" + failure.target() + "&c in table &4" + failure.table() + "&c: &4" + failure.value(), failure.exception());
    }

    @Override
    public void saveCacheImpl(@NotNull String table, @NotNull String target) {
        final Map<String, ConcurrentHashMap<String, CachedValue>> tableMap = cache.get(table);
        if (tableMap == null) return;
        final ConcurrentHashMap<String, CachedValue> targetMap = tableMap.get(target);
        if (targetMap != null) for (final FailedSet failure : setToDatabase(table, target, targetMap)) dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to save cached &4" + failure.column() + "&c for &4" + failure.target() + "&c in table &4" + failure.table() + "&c: &4" + failure.value(), failure.exception());
    }

    @Override @NotNull
    public Optional<MigrationData> getMigrationDataFromDatabaseImpl(@NotNull DataManager newManager) {
        final Map<String, Set<String>> tablesKeys = new HashMap<>(); // {Table, Keys}
        final ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>>> values = new ConcurrentHashMap<>(); // {Table, {Target, {Key, Value}}}
        final int oldPrefixLength = dataManager.tablePrefix.length();
        for (final Table<?> table : dsl.meta().getTables()) {
            // Skip tables that don't have target column
            if (table.field(StringData.TARGET_COLUMN) == null) {
                AnnoyingPlugin.log(Level.WARNING, dataManager.storageConfig.getMigrationLogPrefix() + "Table &4" + table.getName() + "&c doesn't have a '&4target&c' key, skipping...");
                continue;
            }
            final String tableWithoutPrefix = table.getName().substring(oldPrefixLength);

            // Get all keys
            final Set<String> keys = new HashSet<>();
            for (final Field<?> field : table.fields()) keys.add(field.getName());
            tablesKeys.put(tableWithoutPrefix, keys);

            // Get values for each target
            final ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>> tableValues = new ConcurrentHashMap<>(); // {Target, {Key, Value}}
            for (final Record record : dsl.selectFrom(table).fetch()) {
                // Skip records without a target
                final String target = (String) record.get(StringData.TARGET_COLUMN);
                if (target == null) continue;

                // Add record's values to tableValues
                final ConcurrentHashMap<String, CachedValue> keyValues = new ConcurrentHashMap<>(); // {Key, Value}
                for (final Map.Entry<String, Object> entry : record.intoMap().entrySet()) {
                    final String key = entry.getKey();
                    if (!key.equals(StringData.TARGET_COLUMN)) keyValues.put(key, new CachedValue((String) entry.getValue()));
                }
                tableValues.put(target, keyValues);
            }
            if (!tableValues.isEmpty()) values.put(newManager.getTableName(tableWithoutPrefix), tableValues);
        }

        return Optional.of(new MigrationData(tablesKeys, values));
    }

    @Override @NotNull
    protected Optional<String> getFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        try {
            return dsl
                    .selectFrom(table)
                    .where(StringData.TARGET_COLUMN, target)
                    .fetchOptional()
                    .map(record -> record.get(key, String.class));
        } catch (final DataAccessException e) {
            dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to get value for &4" + key + "&c in table &4" + table + "&c for &4" + target, e);
            return Optional.empty();
        }
    }

    @Override @Nullable
    protected FailedSet setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        try {
            dsl
                    .insertInto(DSL.table(table))
                    .set(DSL.field(StringData.TARGET_COLUMN), target)
                    .set(DSL.field(key), value)
                    .onDuplicateKeyUpdate()
                    .set(DSL.field(key), value)
                    .execute();
            return null;
        } catch (final DataAccessException e) {
            return new FailedSet(table, target, key, value, e);
        }
    }

    @Override @NotNull
    protected Set<FailedSet> setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull ConcurrentHashMap<String, CachedValue> data) {
        InsertSetMoreStep<Record> insertStep = dsl
                .insertInto(DSL.table(table))
                .set(DSL.field(StringData.TARGET_COLUMN), target);
        for (final ConcurrentHashMap.Entry<String, CachedValue> entry : data.entrySet()) {
            insertStep = insertStep.set(DSL.field(entry.getKey()), entry.getValue().value());
        }

        // Execute
        try {
            insertStep.execute();
            return Collections.emptySet();
        } catch (final DataAccessException e) {
            final Set<FailedSet> failed = new HashSet<>();
            for (final ConcurrentHashMap.Entry<String, CachedValue> entry : data.entrySet()) failed.add(new FailedSet(table, target, entry.getKey(), entry.getValue().value(), e));
            return failed;
        }
    }

    @Override
    protected boolean removeFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        try {
            dsl
                    .update(DSL.table(table))
                    .setNull(DSL.field(key))
                    .where(DSL.field(StringData.TARGET_COLUMN, String.class).eq(target))
                    .execute();
            return true;
        } catch (final DataAccessException e) {
            dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to remove value for &4" + key + "&c in table &4" + table + "&c for &4" + target, e);
            return false;
        }
    }

    /**
     * Create the given tables and keys in the database
     *
     * @param   tablesKeys   the tables and keys to create
     */
    public void createTablesKeys(@NotNull Map<String, Set<String>> tablesKeys) {
        for (final Map.Entry<String, Set<String>> entry : tablesKeys.entrySet()) {
            final String table = dataManager.getTableName(entry.getKey());

            // Create table if missing
            try {
                dsl
                        .createTableIfNotExists(table)
                        .column(StringData.TARGET_COLUMN, SQLDataType.VARCHAR(255))
                        .constraints(DSL.constraint().primaryKey(StringData.TARGET_COLUMN))
                        .execute();
            } catch (final DataAccessException e) {
                dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to create table &4" + table, e);
                continue;
            }

            // Create missing columns
            for (final String key : entry.getValue()) {
                final String keyLower = key.toLowerCase();
                if (!keyLower.equals(StringData.TARGET_COLUMN)) try {
                    dsl
                            .alterTableIfExists(table)
                            .addColumnIfNotExists(keyLower, SQLDataType.CLOB)
                            .execute();
                } catch (final DataAccessException e) {
                    dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to create key &4" + keyLower + "&c in table &4" + table, e);
                }
            }
        }
    }
}
