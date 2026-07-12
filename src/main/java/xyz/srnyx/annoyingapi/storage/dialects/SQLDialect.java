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
import xyz.srnyx.annoyingapi.storage.CachedValue;
import xyz.srnyx.annoyingapi.storage.ConnectionException;
import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.storage.FailedSet;
import xyz.srnyx.annoyingapi.storage.StorageMethod;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * SQL dialect for a specific type of database
 */
public class SQLDialect extends Dialect {
    private static boolean HIKARI_LOGS_QUIETED = false;
    static {
        // Disable jOOQ startup logs
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
    }

    @NotNull public final HikariDataSource dataSource;
    @NotNull public final DSLContext dsl;
    /**
     * {@code [ Table name: [ Target: [ Data key: Data value ] ] ]}
     * <br>{@code Map<Table name, Map<Target, Map<Data key, Data value>>>}
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
        if (dataManager.storageConfig.method.sqlInfo == null) {
            throw new IllegalStateException("The storage method " + dataManager.storageConfig.method + " is not an SQL method");
        }
        final Path dataPath = dataManager.storageConfig.plugin.getDataFolder().toPath();

        // Get url & properties
        String url = dataManager.storageConfig.method.sqlInfo.url().apply(dataPath);
        final Properties properties = new Properties();
        if (dataManager.storageConfig.method.isSQLRemote()) {
            url += dataManager.storageConfig.remote_connection.host + ":" + dataManager.storageConfig.remote_connection.port + "/" + dataManager.storageConfig.remote_connection.database;
            properties.putAll(dataManager.storageConfig.remote_connection.properties);
            if (!dataManager.storageConfig.remote_connection.username.isEmpty()) properties.setProperty("user", dataManager.storageConfig.remote_connection.username);
            if (!dataManager.storageConfig.remote_connection.password.isEmpty()) properties.setProperty("password", dataManager.storageConfig.remote_connection.password);
        }
        final String finalUrl = url;

        // Load required libraries
        if (dataManager.plugin.libraryManager != null) {
            // Load HikariCP library
            if (!dataManager.plugin.libraryManager.loadLibrary(AnnoyingAPILibrary.HIKARICP)) {
                throw new ConnectionException("Failed to download HikariCP library for " + dataManager.storageConfig.method, finalUrl, properties);
            }

            // Load driver's required library
            if (dataManager.storageConfig.method.sqlInfo.library() != null && !dataManager.plugin.libraryManager.loadLibrary(dataManager.storageConfig.method.sqlInfo.library())) {
                throw new ConnectionException("Failed to download required library " + dataManager.storageConfig.method.sqlInfo.library().getId() + " for " + dataManager.storageConfig.method, finalUrl, properties);
            }
        }

        // Quiet HikariCP's lifecycle logs
        if (!HIKARI_LOGS_QUIETED) {
            HIKARI_LOGS_QUIETED = true;
            try {
                final Class<?> configurator = Class.forName("org.apache.logging.log4j.core.config.Configurator");
                final Class<?> level = Class.forName("org.apache.logging.log4j.Level");
                final Object WARN = level.getField("WARN").get(null);
                configurator.getMethod("setLevel", String.class, level).invoke(null, HikariDataSource.class.getPackageName(), WARN);
            } catch (final ReflectiveOperationException ignored) {}
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
        if (dataManager.plugin.libraryManager != null && dataManager.storageConfig.method.sqlInfo.library() != null) {
            // Get IsolatedClassLoader of library
            final IsolatedClassLoader classLoader;
            try {
                classLoader = dataManager.plugin.libraryManager.loadLibraryIsolated(dataManager.storageConfig.method.sqlInfo.library());
            } catch (final Exception e) {
                throw new ConnectionException(e, finalUrl, properties);
            }
            if (classLoader == null) throw new ConnectionException("Failed to load library for " + dataManager.storageConfig.method, finalUrl, properties);

            // Get driver class
            final Method connectMethod;
            final Driver driver;
            try {
                final Class<?> driverClass = classLoader.loadClass(dataManager.storageConfig.method.sqlInfo.driver());
                connectMethod = driverClass.getMethod("connect", String.class, Properties.class);
                driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new ConnectionException(e, finalUrl, properties);
            }

            // Create DSL from IsolatedClassLoader
            hikariConfig.setDataSource(new DataSource() {
                @Override @NotNull
                public Connection getConnection() throws SQLException {
                    try {
                        return (Connection) connectMethod.invoke(driver, finalUrl, properties);
                    } catch (final Exception e) {
                        throw new SQLException(e);
                    }
                }

                @Override @NotNull
                public Connection getConnection(String username, String password) throws SQLFeatureNotSupportedException {
                    throw new SQLFeatureNotSupportedException("getConnection(String, String) is not supported for IsolatedClassLoader of custom data source implementation");
                }
                @Override @NotNull
                public PrintWriter getLogWriter() throws SQLFeatureNotSupportedException {
                    throw new SQLFeatureNotSupportedException("getLogWriter() is not supported for IsolatedClassLoader of custom data source implementation");
                }
                @Override
                public void setLogWriter(PrintWriter out) {
                    dataManager.plugin.logErrorTrack(Level.WARNING, "&4setLogWriter(PrintWriter)&c is not supported for IsolatedClassLoader of custom data source implementation");
                }
                @Override
                public void setLoginTimeout(int seconds) {}
                @Override
                public int getLoginTimeout() {
                    return 0;
                }
                @Override @NotNull
                public <T> T unwrap(Class<T> iface) throws SQLException {
                    throw new SQLException("Not a wrapper");
                }
                @Override
                public boolean isWrapperFor(Class<?> iface) {
                    return false;
                }
                @Override @NotNull
                public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                    throw new SQLFeatureNotSupportedException("getParentLogger() is not supported for IsolatedClassLoader of custom data source implementation");
                }
            });
        } else {
            // Driver class already exists on classpath
            hikariConfig.setDriverClassName(dataManager.storageConfig.method.sqlInfo.driver());
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

    @Override @NotNull
    public Stats getStats() {
        long cacheTargets = 0L;
        long cacheValues = 0L;
        for (final ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>> table : cache.values()) {
            cacheTargets += table.size();
            if (!table.isEmpty()) for (final ConcurrentHashMap<String, CachedValue> target : table.values()) cacheValues += target.size();
        }
        return new Stats(cacheTargets, cacheValues);
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
            // Skip tables that don't start with the old prefix
            final String tableName = table.getName();
            if (!tableName.startsWith(dataManager.tablePrefix)) {
                AnnoyingPlugin.log(Level.WARNING, dataManager.storageConfig.getMigrationLogPrefix() + "Table &4" + tableName + "&c doesn't start with the old prefix &4" + dataManager.tablePrefix + "&c, skipping...");
                continue;
            }

            // Skip tables that don't have target column
            if (table.field(StringData.TARGET_COLUMN) == null) {
                AnnoyingPlugin.log(Level.WARNING, dataManager.storageConfig.getMigrationLogPrefix() + "Table &4" + tableName + "&c doesn't have a '&" + StringData.TARGET_COLUMN + "&c' key, skipping...");
                continue;
            }

            final String tableWithoutPrefix =tableName.substring(oldPrefixLength);

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
                    .selectFrom(table(table))
                    .where(targetField().eq(target))
                    .fetchOptional()
                    .map(record -> record.get(key, String.class));
        } catch (final DataAccessException e) {
            dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to get value for &4" + key + "&c in table &4" + table + "&c for &4" + target, e);
            return Optional.empty();
        }
    }

    @Override @Nullable
    protected FailedSet setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        return setToDatabaseImpl(table, target, Map.of(key, value)).stream()
                .findFirst().orElse(null);
    }

    @Override @NotNull
    protected List<FailedSet> setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) {
        try {
            upsert(table, target, data);
            return Collections.emptyList();
        } catch (final DataAccessException e) {
            final List<FailedSet> failed = new ArrayList<>();
            for (final Map.Entry<String, String> entry : data.entrySet()) failed.add(new FailedSet(table, target, entry.getKey(), entry.getValue(), e));
            return failed;
        }
    }

    /**
     * Update given columns for target if it already exists, otherwise insert new row
     * <br>Done as explicit UPDATE-then-INSERT-if-missing rather than jOOQ's {@code onDuplicateKeyUpdate}/{@code onConflict}
     * emulation, since that emulation requires real primary key metadata that a dynamically-named {@link DSL#table(String)}
     * doesn't carry (this fails on H2 with "cannot be emulated when inserting into non-updatable tables")
     *
     * @param   table   the table to upsert into
     * @param   target  the target to upsert
     * @param   values  the column/value pairs to upsert
     *
     * @throws  DataAccessException if a database access error occurs
     */
    private void upsert(@NotNull String table, @NotNull String target, @NotNull Map<String, String> values) {
        final Table<Record> tableRecord = table(table);
        final Field<String> targetField = targetField();
        final Map<Field<String>, String> fieldMap = new LinkedHashMap<>(values.size());
        for (final Map.Entry<String, String> entry : values.entrySet()) fieldMap.put(field(entry.getKey()), entry.getValue());

        // Update existing row, return if exist/updated
        if (dsl.update(tableRecord)
                .set(fieldMap)
                .where(targetField.eq(target))
                .execute() != 0) return;

        // No existing row, insert new row
        final Map<Field<String>, String> insertMap = new LinkedHashMap<>(fieldMap);
        insertMap.put(targetField, target);
        dsl.insertInto(tableRecord)
                .set(insertMap)
                .execute();
    }

    @Override
    protected boolean removeFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        try {
            dsl
                    .update(table(table))
                    .setNull(field(key))
                    .where(targetField().eq(target))
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
                        .createTableIfNotExists(table(table))
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
                            .alterTableIfExists(table(table))
                            .addColumnIfNotExists(keyLower, SQLDataType.CLOB)
                            .execute();
                } catch (final DataAccessException e) {
                    dataManager.plugin.logErrorTrack(Level.SEVERE, "&cFailed to create key &4" + keyLower + "&c in table &4" + table, e);
                }
            }
        }
    }

    /**
     * Warms up jOOQ's SELECT/UPDATE/INSERT code paths for the given table so the classloading/JIT
     * cost of first use doesn't impact real {@code get}/{@code set}/{@code remove} call
     * <br>Uses a sentinel target that can never match a real row, so nothing is ever written or needs cleaning up
     *
     * @param   rawTable    the (unprefixed) table to warm up against
     */
    public void warmup(@NotNull String rawTable) {
        try {
            final Table<Record> tableRecord = table(dataManager.getTableName(rawTable));
            final Field<String> targetField = targetField();
            final String sentinel = "__annoyingapi_warmup__";

            // SELECT: reads have no side effects
            dsl.selectFrom(tableRecord).where(targetField.eq(sentinel)).fetchOptional();

            // UPDATE: sentinel can never match real row, so it always affects 0 rows
            dsl.update(tableRecord).set(Map.of(targetField, sentinel)).where(targetField.eq(sentinel)).execute();

            // INSERT: build only (no execute), so no row is ever written
            dsl.insertInto(tableRecord).set(Map.of(targetField, sentinel));
        } catch (final DataAccessException ignored) {}
    }

    @NotNull
    private static Table<Record> table(@NotNull String name) {
        return DSL.table(DSL.name(name));
    }

    @NotNull
    private static Field<String> field(@NotNull String name) {
        return DSL.field(DSL.name(name), String.class);
    }
    
    @NotNull
    private static Field<String> targetField() {
        return field(StringData.TARGET_COLUMN);
    }
}
