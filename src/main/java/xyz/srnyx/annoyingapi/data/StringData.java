package xyz.srnyx.annoyingapi.data;

import org.bukkit.OfflinePlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.options.DataOptions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


/**
 * A data class for storing and retrieving string data from SQL databases
 */
public class StringData extends Data<String> {
    /**
     * The primary target column name
     */
    @NotNull public static final String TARGET_COLUMN = "target";

    /**
     * The {@link DataManager data manager} instance
     */
    @NotNull private final DataManager dataManager;
    /**
     * The name of the table in the database to get/store the data from/to
     */
    @NotNull private final String table;
    /**
     * Whether to use the cache for this data
     * <br>If {@code false}, all cache methods will return {@code null}
     *
     * @see #getCache()
     * @see #getFromCache(String)
     * @see #saveCache()
     */
    public final boolean useCache;

    /**
     * Construct a new {@link StringData} for the given string
     *
     * @param   plugin      {@link #plugin}
     * @param   table       {@link #table}
     * @param   string      {@link #target}
     * @param   useCache    {@link #useCache}, or {@code null} to use {@link DataOptions#useCacheDefault}
     */
    public StringData(@NotNull AnnoyingPlugin plugin, @NotNull String table, @NotNull String string, @Nullable Boolean useCache) {
        super(plugin, string);
        if (plugin.dataManager == null) throw new IllegalStateException(plugin.options.dataOptions.enabled ? "Data manager is not initialized!" : "Data manager is not enabled! Plugin devs: enable it by setting options.dataOptions.enabled to true");
        this.dataManager = plugin.dataManager;
        this.table = dataManager.getTableName(table);
        this.useCache = dataManager.storageConfig.cache.enabled && (useCache == null ? plugin.options.dataOptions.useCacheDefault : useCache);
    }

    /**
     * Construct a new {@link StringData} for the given string with {@link DataOptions#useCacheDefault} as {@link #useCache}
     *
     * @param   plugin  {@link #plugin}
     * @param   table   {@link #table}
     * @param   string  {@link #target}
     */
    public StringData(@NotNull AnnoyingPlugin plugin, @NotNull String table, @NotNull String string) {
        this(plugin, table, string, null);
    }

    /**
     * Construct a new {@link StringData} for the given {@link OfflinePlayer}
     * <br>This uses the same table used for {@link EntityData} ({@link EntityData#TABLE_NAME}) and the player's UUID as the target
     *
     * @param   plugin  {@link #plugin}
     * @param   player  the player to get/store the data for
     * @param   useCache    {@link #useCache}, or {@code null} to use {@link DataOptions#useCacheDefault}
     */
    public StringData(@NotNull AnnoyingPlugin plugin, @NotNull OfflinePlayer player, @Nullable Boolean useCache) {
        this(plugin, EntityData.TABLE_NAME, player.getUniqueId().toString(), useCache);
    }

    /**
     * Construct a new {@link StringData} for the given {@link OfflinePlayer} with {@link DataOptions#useCacheDefault} as {@link #useCache}
     * <br>This uses the same table used for {@link EntityData} ({@link EntityData#TABLE_NAME}) and the player's UUID as the target
     *
     * @param   plugin  {@link #plugin}
     * @param   player  the player to get/store the data for
     */
    public StringData(@NotNull AnnoyingPlugin plugin, @NotNull OfflinePlayer player) {
        this(plugin, player, null);
    }

    @Override @Nullable
    public String get(@NotNull String key) {
        // Get the data from the cache
        if (useCache) {
            final String cached = getFromCache(key);
            if (cached != null) return cached;
        }

        // Get the data from the database
        try {
            final ResultSet result = dataManager.dialect.getValue(table, target, key).executeQuery();
            String string = null;
            if (result.next()) string = result.getString(1);
            result.close();

            // Cache the data
            if (useCache) setToCache(key, string);

            return string;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to get " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!", e);
        }
        return null;
    }

    @Override
    protected boolean set(@NotNull String key, @NotNull String value) {
        // Set the data in the cache
        if (useCache) {
            setToCache(key, value);
            return true;
        }

        // Set the data in the database
        try (final PreparedStatement statement = dataManager.dialect.setValue(table, target, key, value)) {
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to set " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!", e);
            return false;
        }
    }

    @Override
    public boolean remove(@NotNull String key) {
        // Remove the data from the cache
        if (useCache) {
            removeFromCache(key);
            return true;
        }

        // Remove the data from the database
        try (final PreparedStatement statement = dataManager.dialect.removeValue(table, target, key)) {
            statement.executeUpdate();
            return true;
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to remove " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!", e);
            return false;
        }
    }

    /**
     * Get the target's cached data for the given key
     *
     * @param   key the key to get from the cache
     *
     * @return      the target's cached data for the given key, or {@code null} if the cache is disabled or the target has no cached data for the key
     */
    @Nullable
    public String getFromCache(@NotNull String key) {
        final Map<String, String> data = getCache();
        return data == null ? null : data.get(key);
    }

    /**
     * Set the target's cached data for the given key
     *
     * @param   key     the key to set in the cache
     * @param   value   the value to set in the cache
     *
     * @return          this {@link StringData} instance
     */
    @NotNull
    public StringData setToCache(@NotNull String key, @Nullable String value) {
        if (value == null) return removeFromCache(key);
        Map<String, String> data = getCache();
        if (data == null) {
            data = new HashMap<>();
            dataManager.dataCache.computeIfAbsent(table, k -> new HashMap<>()).put(target, data);
        }
        data.put(key, value);
        return this;
    }

    /**
     * Remove the target's cached data for the given key
     *
     * @param   key the key to remove from the cache
     *
     * @return      this {@link StringData} instance
     */
    @NotNull
    public StringData removeFromCache(@NotNull String key) {
        final Map<String, String> data = getCache();
        if (data != null) data.remove(key);
        return this;
    }

    /**
     * Get the target's cached data
     *
     * @return  the target's cached data, or {@code null} if the cache is disabled or the target has no cached data
     */
    @Nullable
    public Map<String, String> getCache() {
        if (!useCache) return null;
        final Map<String, Map<String, String>> tableData = dataManager.dataCache.get(table);
        return tableData == null ? null : tableData.get(target);
    }

    /**
     * Save the target's cached data to the database
     *
     * @return  {@code true} if the data was saved successfully, {@code false} otherwise
     */
    public boolean saveCache() {
        final Map<String, String> data = getCache();
        if (data != null) try (final PreparedStatement statement = dataManager.dialect.setValues(table, target, data)) {
            statement.executeUpdate();
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to save cached data for target " + target + " in table " + table + ": " + data, e);
            return false;
        }
        return true;
    }
}
