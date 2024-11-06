package xyz.srnyx.annoyingapi.storage.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.storage.FailedSet;
import xyz.srnyx.annoyingapi.storage.Value;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


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
    @Nullable
    public Value getFromCache(@NotNull String table, @NotNull String target, @NotNull String key) {
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
    public void setToCache(@NotNull String table, @NotNull String target, @NotNull String key, @Nullable Value value) {
        if (value == null) {
            markRemovedInCache(table, target, key);
            return;
        }
        setToCacheImpl(table, target, key, value);
    }

    /**
     * Mark a value as removed in the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     */
    public void markRemovedInCache(@NotNull String table, @NotNull String target, @NotNull String key) {
        markRemovedInCacheImpl(table, target, key);
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
     * @return          the failed value information, null if successful
     */
    @Nullable
    public final FailedSet setToDatabase(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value) {
        return setToDatabaseImpl(table, target, key.toLowerCase(), value);
    }

    /**
     * Set a value to the database
     *
     * @param   table   the table
     * @param   target  the target
     * @param   data    the data to set
     *
     * @return          set of failed values as {@link FailedSet FailedSets}
     */
    @NotNull
    public final Set<FailedSet> setToDatabase(@NotNull String table, @NotNull String target, @NotNull ConcurrentHashMap<String, Value> data) {
        final ConcurrentHashMap<String, Value> dataLower = new ConcurrentHashMap<>();
        for (final ConcurrentHashMap.Entry<String, Value> entry : data.entrySet()) dataLower.put(entry.getKey().toLowerCase(), entry.getValue());
        return setToDatabaseImpl(table, target, dataLower);
    }

    /**
     * Set multiple values to the database
     *
     * @param   data    the data to set
     *
     * @return          set of failed values as {@link FailedSet FailedSets}
     */
    @NotNull
    public final Set<FailedSet> setToDatabase(@NotNull Map<String, Map<String, ConcurrentHashMap<String, Value>>> data) {
        final Set<FailedSet> failed = new HashSet<>();
        for (final Map.Entry<String, Map<String, ConcurrentHashMap<String, Value>>> entry : data.entrySet()) {
            final String table = entry.getKey();
            for (final Map.Entry<String, ConcurrentHashMap<String, Value>> entry1 : entry.getValue().entrySet()) {
                final Set<FailedSet> failedSet = setToDatabase(table, entry1.getKey(), entry1.getValue());
                if (!failedSet.isEmpty()) failed.addAll(failedSet);
            }
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
     * @return          the value inside a {@link Value}, null if it isn't cached
     */
    @Nullable
    protected abstract Value getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key);

    /**
     * Set a value to the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     * @param   value   the value inside a {@link Value}
     */
    protected abstract void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull Value value);

    /**
     * Mark a value as removed in the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     */
    protected abstract void markRemovedInCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key);

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
     * @return          the failed value information, null if successful
     */
    @Nullable
    protected abstract FailedSet setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull String value);

    /**
     * Set multiple values to the database
     *
     * @param   table   the table to set to
     * @param   target  the target to set to
     * @param   data    the data to set
     *
     * @return          set of failed values as {@link FailedSet FailedSets}
     */
    @NotNull
    protected abstract Set<FailedSet> setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull ConcurrentHashMap<String, Value> data);

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
        @NotNull public final Map<String, Map<String, ConcurrentHashMap<String, Value>>> data;

        /**
         * Construct a new {@link MigrationData} with the given data
         *
         * @param   tablesKeys  {@link #tablesKeys}
         * @param   data        {@link #data}
         */
        public MigrationData(@NotNull Map<String, Set<String>> tablesKeys, @NotNull Map<String, Map<String, ConcurrentHashMap<String, Value>>> data) {
            this.tablesKeys = tablesKeys;
            this.data = data;
        }
    }
}
