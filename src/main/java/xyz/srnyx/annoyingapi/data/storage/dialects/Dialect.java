package xyz.srnyx.annoyingapi.data.storage.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.data.storage.DataManager;

import java.util.*;


/**
 * Dialect for a specific type of database
 */
public abstract class Dialect {
    /**
     * The {@link DataManager} to use for database operations
     */
    @NotNull protected final DataManager dataManager;

    /**
     * Construct a new {@link Dialect} with the given {@link DataManager}
     *
     * @param   dataManager {@link #dataManager}
     */
    public Dialect(@NotNull DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Get a value from the cache
     *
     * @param   table   the table to get from
     * @param   target  the target to get from
     * @param   key     the key to get
     *
     * @return          the value, empty if not found
     */
    @NotNull
    public Optional<String> getFromCache(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getFromCacheImpl(table, target, key);
    }

    /**
     * Set a value to the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     * @param   value   the value
     */
    public void setToCache(@NotNull String table, @NotNull String target, @NotNull String key, @Nullable String value) {
        if (value == null) {
            removeFromCache(table, target, key);
            return;
        }
        setToCacheImpl(table, target, key, value);
    }

    /**
     * Remove a value from the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     */
    public void removeFromCache(@NotNull String table, @NotNull String target, @NotNull String key) {
        removeFromCacheImpl(table, target, key);
    }

    /**
     * Save all cache data to the database
     */
    public void saveCache() {
        saveCacheImpl();
    }

    /**
     * Save a specific target in a table to the cache
     *
     * @param   table   the table to save in
     * @param   target  the target to save
     */
    public void saveCache(@NotNull String table, @NotNull String target) {
        saveCacheImpl(table, target);
    }

    /**
     * Get migration data from the database
     *
     * @param   newManager  the new {@link DataManager} to migrate to
     *
     * @return              the migration data, empty if something went wrong
     */
    @NotNull
    public final Optional<MigrationData> getMigrationDataFromDatabase(@NotNull DataManager newManager) {
        return getMigrationDataFromDatabaseImpl(newManager);
    }

    /**
     * Get a value from the database
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     *
     * @return          the value, empty if not found
     */
    @NotNull
    public final Optional<String> getFromDatabase(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getFromDatabaseImpl(table, target, key.toLowerCase());
    }

    /**
     * Set a value to the database
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     * @param   value   the value
     *
     * @return          true if the value was successfully set, false otherwise
     */
    public final boolean setToDatabase(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        return setToDatabaseImpl(table, target, key.toLowerCase(), value);
    }

    /**
     * Set a value to the database
     *
     * @param   table   the table
     * @param   target  the target
     * @param   data    the data to set
     *
     * @return          a map of failed values
     */
    public final boolean setToDatabase(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data) {
        final Map<String, String> dataLower = new HashMap<>();
        for (final Map.Entry<String, String> entry : data.entrySet()) dataLower.put(entry.getKey().toLowerCase(), entry.getValue());
        return setToDatabaseImpl(table, target, dataLower);
    }

    /**
     * Set multiple values to the database
     *
     * @param   data    the data to set
     *
     * @return          a map of failed values
     */
    @NotNull
    public final Map<String, Map<String, Map<String, String>>> setToDatabase(@NotNull Map<String, Map<String, Map<String, String>>> data) {
        final Map<String, Map<String, Map<String, String>>> failed = new HashMap<>();
        for (final Map.Entry<String, Map<String, Map<String, String>>> entry : data.entrySet()) {
            final Map<String, Map<String, String>> failed1 = new HashMap<>();
            final String table = entry.getKey();
            for (final Map.Entry<String, Map<String, String>> entry1 : entry.getValue().entrySet()) {
                final String target = entry1.getKey();
                final Map<String, String> values = entry1.getValue();
                if (!setToDatabase(table, target, values)) failed1.put(target, values);
            }
            if (!failed1.isEmpty()) failed.put(table, failed1);
        }
        return failed;
    }

    /**
     * Remove a value from the database
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     *
     * @return          true if the value was successfully removed, false otherwise
     */
    public final boolean removeValueFromDatabase(@NotNull String table, @NotNull String target, @NotNull String key) {
        return removeFromDatabaseImpl(table, target, key.toLowerCase());
    }

    /**
     * Get a value from the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     *
     * @return          the value, empty if not found
     */
    @NotNull
    protected abstract Optional<String> getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key);

    /**
     * Set a value to the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     * @param   value   the value
     */
    protected abstract void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value);

    /**
     * Remove a value from the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     */
    protected abstract void removeFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key);

    /**
     * Save all cache data to the database
     */
    protected abstract void saveCacheImpl();

    /**
     * Save a specific target in a table to the cache
     *
     * @param   table   the table to save in
     * @param   target  the target to save
     */
    protected abstract void saveCacheImpl(@NotNull String table, @NotNull String target);

    /**
     * Get migration data from the database
     *
     * @param   newManager  the new {@link DataManager} to migrate to
     *
     * @return              the migration data, empty if something went wrong
     */
    @NotNull
    protected abstract Optional<MigrationData> getMigrationDataFromDatabaseImpl(@NotNull DataManager newManager);

    /**
     * Get a value from the database
     *
     * @param   table   the table to get from
     * @param   target  the target to get from
     * @param   key     the key to get
     *
     * @return          the value, empty if not found
     */
    @NotNull
    protected abstract Optional<String> getFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key);

    /**
     * Set a value to the database
     *
     * @param   table   the table to set to
     * @param   target  the target to set to
     * @param   key     the key to set
     * @param   value   the value to set
     *
     * @return          true if the value was successfully set, false otherwise
     */
    protected abstract boolean setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value);

    /**
     * Set multiple values to the database
     *
     * @param   table   the table to set to
     * @param   target  the target to set to
     * @param   data    the data to set
     *
     * @return          true if all values were successfully set, false otherwise
     */
    protected abstract boolean setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data);

    /**
     * Remove a value from the database
     *
     * @param   table   the table to remove from
     * @param   target  the target to remove from
     * @param   key     the key to remove
     *
     * @return          true if the value was successfully removed, false otherwise
     */
    protected abstract boolean removeFromDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key);

    /**
     * Data for a database migration
     *
     * @see #getMigrationDataFromDatabase(DataManager)
     */
    public static class MigrationData {
        /**
         * [table, keys]
         */
        @NotNull public final Map<String, Set<String>> tablesKeys;
        /**
         * [table, [target, [key, value]]]
         */
        @NotNull public final Map<String, Map<String, Map<String, String>>> data;

        /**
         * Construct a new {@link MigrationData} with the given data
         *
         * @param   tablesKeys  {@link #tablesKeys}
         * @param   data        {@link #data}
         */
        public MigrationData(@NotNull Map<String, Set<String>> tablesKeys, @NotNull Map<String, Map<String, Map<String, String>>> data) {
            this.tablesKeys = tablesKeys;
            this.data = data;
        }
    }
}
