package xyz.srnyx.annoyingapi.data;

import org.bukkit.Bukkit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.dialects.SQLDialect;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;


/**
 * The data manager for the plugin, used to manage the connection and data storage
 */
public class DataManager {
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
     * @param   file                the {@link AnnoyingFile file} to get the connection information from
     *
     * @throws ConnectionException if the connection to the database fails for any reason
     */
    public DataManager(@NotNull AnnoyingFile file) throws ConnectionException {
        this.storageConfig = new StorageConfig(file);
        connection = storageConfig.createConnection();
        dialect = storageConfig.method.dialect.apply(this);
        tablePrefix = storageConfig.remoteConnection != null ? storageConfig.remoteConnection.tablePrefix : "";
    }

    /**
     * Create the given tables and columns in the database
     *
     * @param   tablesColumns   the tables and columns to create
     */
    public void createTablesColumns(@NotNull Map<String, Set<String>> tablesColumns) {
        for (final Map.Entry<String, Set<String>> entry : tablesColumns.entrySet()) {
            final String table = entry.getKey();
            // Create table
            try (final PreparedStatement tableStatement = dialect.createTable(table)) {
                tableStatement.executeUpdate();
            } catch (final SQLException e) {
                AnnoyingPlugin.log(Level.SEVERE, "&cFailed to create table &4" + table, e);
                continue;
            }
            // Create columns
            for (final String column : entry.getValue()) {
                final String columnLower = column.toLowerCase();
                if (!columnLower.equals(StringData.TARGET_COLUMN)) try (final PreparedStatement columnStatement = dialect.createColumn(table, columnLower)) {
                    if (columnStatement != null) columnStatement.executeUpdate();
                } catch (final SQLException e) {
                    AnnoyingPlugin.log(Level.SEVERE, "&cFailed to create column &4" + columnLower + "&c in table &4" + table, e);
                }
            }
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
        return tablePrefix + tableName.toLowerCase();
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
     * Starts the asynchronous task to save the cache on an interval
     *
     * @param   plugin      the plugin to run the task on
     * @param   interval    the interval in ticks to save the cache
     */
    public void startCacheSavingOnInterval(@NotNull AnnoyingPlugin plugin, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveCache, interval, interval);
    }

    /**
     * Saves the data from the cache to the database
     */
    public void saveCache() {
        for (final SQLDialect.SetValueStatement statement : dialect.setValues(dataCache)) try {
            statement.statement.executeUpdate();
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to save cached values for &4" + statement.target + "&c in table &4" + statement.table + "&c: &4" + statement.values, e);
        }
    }
}
