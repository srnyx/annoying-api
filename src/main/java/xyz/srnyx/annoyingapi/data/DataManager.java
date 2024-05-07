package xyz.srnyx.annoyingapi.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.dialects.SQLDialect;
import xyz.srnyx.annoyingapi.options.DataOptions;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


/**
 * The data manager for the plugin, used to manage the connection and data storage
 */
public class DataManager {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull public final AnnoyingPlugin plugin;
    /**
     * The {@link StorageConfig storage.yml config} for the plugin
     */
    @NotNull public final StorageConfig storageConfig;
    /**
     * The {@link Connection connection} to the database
     */
    @NotNull public final Connection connection;
    /**
     * The {@link SQLDialect SQL dialect} for the database
     */
    @NotNull public final SQLDialect dialect;
    /**
     * The table prefix for the database (only for remote connections)
     */
    @NotNull public final String tablePrefix;
    /**
     * Cached data from {@link StringData}
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
    @NotNull public final Map<String, Map<String, Map<String, String>>> dataCache = new HashMap<>();

    /**
     * Connect to the configured database and create the pre-defined tables/columns
     *
     * @param   plugin              {@link #plugin}
     *
     * @throws  ConnectionException if the connection to the database fails for any reason
     */
    public DataManager(@NotNull AnnoyingPlugin plugin) throws ConnectionException {
        this.plugin = plugin;
        storageConfig = new StorageConfig(plugin);
        connection = storageConfig.createConnection();
        dialect = storageConfig.method.dialect.apply(this);
        tablePrefix = storageConfig.remoteConnection != null ? storageConfig.remoteConnection.tablePrefix : "";

        // Create tables & columns ahead of time
        plugin.options.dataOptions.tables.forEach((key, value) -> {
            final String table = getTableName(key);
            executeUpdate(dialect.createTable(table), "Failed to create table " + table);
            value.forEach(column -> {
                if (column.equals(StringData.TARGET_COLUMN)) return;
                final String statement = dialect.createColumn(table, column);
                if (statement != null) executeUpdate(statement, "Failed to create column " + column + " in table " + table);
            });
        });

        // Start cache saver interval
        if (plugin.options.dataOptions.cache.saveOn.contains(DataOptions.Cache.SaveOn.INTERVAL)) {
            final long interval = plugin.options.dataOptions.cache.saveOnInterval;
            plugin.attemptTimerAsync(this::saveCache, interval, interval);
        }
    }

    /**
     * Get the full name of a table with the {@link #tablePrefix prefix}
     * <br><i>This just returns the table name if it's a local database or there isn't prefix</i>
     *
     * @param   tableName   the name of the table to get
     *
     * @return              the full name of the table
     */
    @NotNull
    public String getTableName(@NotNull String tableName) {
        return tablePrefix + tableName;
    }

    /**
     * Execute a query on the database
     *
     * @param   query           the query to execute <i>(careful of SQL injection!)</i>
     *
     * @return                  the {@link ResultSet result} of the query
     *
     * @throws  SQLException    if the query fails for any reason
     */
    @NotNull
    public ResultSet executeQuery(@NotNull String query) throws SQLException {
        return connection.createStatement().executeQuery(query);
    }

    /**
     * Execute an update on the database
     *
     * @param   query           the query to execute <i>(careful of SQL injection!)</i>
     * @param   errorMessage    the error message to print if the query fails
     */
    public void executeUpdate(@NotNull String query, @Nullable String errorMessage) {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (final SQLException e) {
            if (errorMessage == null) {
                e.printStackTrace();
                return;
            }
            AnnoyingPlugin.log(Level.SEVERE, errorMessage, e);
        }
    }

    /**
     * Saves the data from the cache to the database
     */
    public void saveCache() {
        dataCache.forEach((table, targets) -> {
            final String tableName = getTableName(table);
            targets.forEach((target, data) -> {
                try (final PreparedStatement statement = dialect.setValues(tableName, target, data)) {
                    statement.execute();
                } catch (final SQLException e) {
                    AnnoyingPlugin.log(Level.SEVERE, "Failed to save cached data for target " + target + " in table " + table + ": " + data, e);
                }
            });
        });
    }
}
