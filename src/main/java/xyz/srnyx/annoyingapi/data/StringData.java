package xyz.srnyx.annoyingapi.data;

import org.bukkit.OfflinePlayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;
import xyz.srnyx.annoyingapi.options.DataOptions;

import java.util.Optional;
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
     * The {@link Dialect} to use for database operations
     */
    @NotNull private final Dialect dialect;
    /**
     * The name of the table in the database to get/store the data from/to
     */
    @NotNull private final String table;
    /**
     * Whether to use the cache for this data
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
        this.dialect = plugin.dataManager.dialect;
        this.table = plugin.dataManager.getTableName(table);
        this.useCache = plugin.dataManager.storageConfig.cache.enabled && (useCache == null ? plugin.options.dataOptions.useCacheDefault : useCache);
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
            final Optional<String> cached = dialect.getFromCache(table, target, key);
            if (cached.isPresent()) return cached.get();
        }

        // Get the data from the database
        final String data = dialect.getFromDatabase(table, target, key).orElse(null);
        if (useCache) dialect.setToCache(table, target, key, data);
        return data;
    }

    @Override
    protected boolean set(@NotNull String key, @NotNull String value) {
        // Set the data in the cache
        if (useCache) {
            dialect.setToCache(table, target, key, value);
            return true;
        }

        // Set the data in the database
        if (!dialect.setToDatabase(table, target, key, value)) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to set " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!");
            return false;
        }
        return true;
    }

    @Override
    public boolean remove(@NotNull String key) {
        // Remove the data from the cache
        if (useCache) {
            dialect.removeFromCache(table, target, key);
            return true;
        }

        // Remove the data from the database
        if (!dialect.removeValueFromDatabase(table, target, key)) {
            AnnoyingPlugin.log(Level.SEVERE, "Failed to remove " + key + " for " + target + " in " + table + ". Make sure you added the table/column to DataOptions!");
            return false;
        }
        return true;
    }
}
