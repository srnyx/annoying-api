package xyz.srnyx.annoyingapi.storage.dialects.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.StringData;
import xyz.srnyx.annoyingapi.storage.ConnectionException;
import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.storage.FailedSet;
import xyz.srnyx.annoyingapi.storage.Value;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


/**
 * SQL dialect for a specific type of database
 */
public abstract class SQLDialect extends Dialect {
    /**
     * The {@link Connection connection} to the database
     */
    @NotNull public final Connection connection;
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
    @NotNull public final Map<String, Map<String, ConcurrentHashMap<String, Value>>> cache = new HashMap<>();

    /**
     * Construct a new {@link SQLDialect} with the given {@link DataManager}
     *
     * @param   dataManager         {@link #dataManager}
     *
     * @throws  ConnectionException if a database connection error occurs
     */
    public SQLDialect(@NotNull DataManager dataManager) throws ConnectionException {
        super(dataManager);
        connection = dataManager.storageConfig.createConnection();
    }

    @Override @Nullable
    public Value getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        final Map<String, ConcurrentHashMap<String, Value>> tableMap = cache.get(table);
        if (tableMap == null) return null;
        final Map<String, Value> targetMap = tableMap.get(target);
        return targetMap == null ? null : targetMap.get(key);
    }

    @Override
    public void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull Value value) {
        cache.computeIfAbsent(table, k -> new HashMap<>()).computeIfAbsent(target, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    @Override
    public void markRemovedInCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key) {
        cache.computeIfAbsent(table, k -> new HashMap<>()).computeIfAbsent(target, k -> new ConcurrentHashMap<>()).put(key, new Value());
    }

    @Override
    public void saveCacheImpl() {
        for (final FailedSet failure : setToDatabase(cache)) AnnoyingPlugin.log(Level.SEVERE, "&cFailed to save cached &4" + failure.column + "&c for &4" + failure.target + "&c in table &4" + failure.table + "&c: &4" + failure.value, failure.exception);
    }

    @Override
    public void saveCacheImpl(@NotNull String table, @NotNull String target) {
        final Map<String, ConcurrentHashMap<String, Value>> tableMap = cache.get(table);
        if (tableMap == null) return;
        final ConcurrentHashMap<String, Value> targetMap = tableMap.get(target);
        if (targetMap != null) for (final FailedSet failure : setToDatabase(table, target, targetMap)) AnnoyingPlugin.log(Level.SEVERE, "&cFailed to save cached &4" + failure.column + "&c for &4" + failure.target + "&c in table &4" + failure.table + "&c: &4" + failure.value, failure.exception);
    }

    @Override @NotNull
    public Optional<MigrationData> getMigrationDataFromDatabaseImpl(@NotNull DataManager newManager) {
        // Get tables
        final Set<String> tables = new HashSet<>();
        try (final PreparedStatement getTables = getTables()) {
            final ResultSet resultSet = getTables.executeQuery();
            while (resultSet.next()) tables.add(resultSet.getString(1));
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, dataManager.storageConfig.migrationLogPrefix + "Failed to get tables!", e);
            return Optional.empty();
        }

        // Get migration data
        final Map<String, Set<String>> tablesKeys = new HashMap<>(); // {Table, Keys}
        final Map<String, Map<String, ConcurrentHashMap<String, Value>>> values = new HashMap<>(); // {Table, {Target, {Key, Value}}}
        final int oldPrefixLength = dataManager.tablePrefix.length();
        for (final String table : tables) {
            final String tableWithoutPrefix = table.substring(oldPrefixLength);
            final Map<String, ConcurrentHashMap<String, Value>> tableValues = new HashMap<>(); // {Target, {Key, Value}}
            try (final PreparedStatement getValues = getAllValuesFromDatabase(table)) {
                final ResultSet resultSet = getValues.executeQuery();

                // Continue if table doesn't have target key
                final Set<String> keys = new HashSet<>();
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int keyCount = metaData.getColumnCount();
                if (keyCount == 0) {
                    AnnoyingPlugin.log(Level.WARNING, dataManager.storageConfig.migrationLogPrefix + "Table &4" + table + "&c has no keys, skipping...");
                    continue;
                }
                for (int i = 1; i <= keyCount; i++) keys.add(metaData.getColumnName(i));
                if (!keys.contains("target")) {
                    AnnoyingPlugin.log(Level.WARNING, dataManager.storageConfig.migrationLogPrefix + "Table &4" + table + "&c doesn't have a '&4target&c' key, skipping...");
                    continue;
                }
                tablesKeys.put(tableWithoutPrefix, keys);

                // Get values for each target
                while (resultSet.next()) {
                    final String target = resultSet.getString("target");
                    if (target == null) continue;
                    final ConcurrentHashMap<String, Value> keyValues = new ConcurrentHashMap<>(); // {Key, Value}
                    for (final String key : keys) if (!key.equals("target")) keyValues.put(key, new Value(resultSet.getString(key)));
                    tableValues.put(target, keyValues);
                }
            } catch (final SQLException e) {
                AnnoyingPlugin.log(Level.SEVERE, dataManager.storageConfig.migrationLogPrefix + "Failed to get values for table &4" + table, e);
            }
            if (!tableValues.isEmpty()) values.put(newManager.getTableName(tableWithoutPrefix), tableValues);
        }

        return Optional.of(new MigrationData(tablesKeys, values));
    }

    /**
     * Create the given tables and keys in the database
     *
     * @param   tablesKeys   the tables and keys to create
     */
    public void createTablesKeys(@NotNull Map<String, Set<String>> tablesKeys) {
        for (final Map.Entry<String, Set<String>> entry : tablesKeys.entrySet()) {
            final String table = dataManager.getTableName(entry.getKey());
            // Create table
            try (final PreparedStatement statement = createTable(table)) {
                statement.executeUpdate();
            } catch (final SQLException e) {
                AnnoyingPlugin.log(Level.SEVERE, "&cFailed to create table &4" + table, e);
                continue;
            }
            // Create keys
            for (final String key : entry.getValue()) {
                final String keyLower = key.toLowerCase();
                if (!keyLower.equals(StringData.TARGET_COLUMN)) try (final PreparedStatement keyStatement = createKeyImpl(table, keyLower)) {
                    if (keyStatement != null) keyStatement.executeUpdate();
                } catch (final SQLException e) {
                    AnnoyingPlugin.log(Level.SEVERE, "&cFailed to create key &4" + keyLower + "&c in table &4" + table, e);
                }
            }
        }
    }

    /**
     * Used for {@link #setToDatabaseImpl(String, String, String, String)} to set the parameters of the {@link PreparedStatement}
     *
     * @param   target          the target to set the value to
     * @param   values          the values to set as {@link Value Values}
     * @param   insertBuilder   the insert query builder
     * @param   valuesBuilder   the values query builder
     * @param   updateBuilder   the update query builder
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database access error occurs
     */
    @NotNull
    protected PreparedStatement setValuesParameters(@NotNull String target, @NotNull List<Value> values, @NotNull StringBuilder insertBuilder, @NotNull StringBuilder valuesBuilder, @Nullable StringBuilder updateBuilder) throws SQLException {
        final boolean hasUpdate = updateBuilder != null;

        // Get query
        final StringBuilder query = insertBuilder.append(valuesBuilder);
        if (hasUpdate) query.append(updateBuilder);

        // Create statement & set parameters
        final PreparedStatement statement = connection.prepareStatement(query.toString());
        statement.setString(1, target);
        final int i = setValuesParameters(statement, values, 2);
        if (hasUpdate) setValuesParameters(statement, values, i);

        return statement;
    }

    /**
     * Set the values parameters of the {@link PreparedStatement} for {@link #setValuesParameters(String, List, StringBuilder, StringBuilder, StringBuilder)}
     *
     * @param   statement       the {@link PreparedStatement} to set the parameters to
     * @param   values          the values to set as {@link Value Values}
     * @param   i               the index to start setting the parameters from
     *
     * @return                  the index after setting the parameters
     *
     * @throws  SQLException    if a database access error occurs
     */
    private int setValuesParameters(@NotNull PreparedStatement statement, @NotNull List<Value> values, int i) throws SQLException {
        for (final Value value : values) if (value.value == null) {
            statement.setNull(i++, Types.VARCHAR);
        } else {
            statement.setString(i++, value.value);
        }
        return i;
    }

    /**
     * Get all tables from the database
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @NotNull
    public final PreparedStatement getTables() throws SQLException {
        return getTablesImpl();
    }

    /**
     * Create a table in the database
     *
     * @param   table           the table to create
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @NotNull
    public final PreparedStatement createTable(@NotNull String table) throws SQLException {
        return createTableImpl(table);
    }

    /**
     * Create a key in the given table (converts the key to lowercase)
     *
     * @param   table           the table to create the key in
     * @param   key             the key to create
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @Nullable
    public final PreparedStatement createKey(@NotNull String table, @NotNull String key) throws SQLException {
        return createKeyImpl(table, key.toLowerCase());
    }

    /**
     * Get all values from the database
     *
     * @param   table           the table to get the values from
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @NotNull
    public final PreparedStatement getAllValuesFromDatabase(@NotNull String table) throws SQLException {
        return getAllValuesFromDatabaseImpl(table);
    }

    /**
     * Get all tables from the database
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @NotNull
    protected abstract PreparedStatement getTablesImpl() throws SQLException;

    /**
     * Create a table in the database
     *
     * @param   table           the table to create
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @NotNull
    protected abstract PreparedStatement createTableImpl(@NotNull String table) throws SQLException;

    /**
     * Create a key in the given table
     *
     * @param   table           the table to create the key in
     * @param   key             the key to create
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @Nullable
    protected abstract PreparedStatement createKeyImpl(@NotNull String table, @NotNull String key) throws SQLException;

    /**
     * Get all values from the database
     *
     * @param   table           the table to get the values from
     *
     * @return                  the {@link PreparedStatement} with the set parameters
     *
     * @throws  SQLException    if a database error occurs
     */
    @NotNull
    protected abstract PreparedStatement getAllValuesFromDatabaseImpl(@NotNull String table) throws SQLException;
}
