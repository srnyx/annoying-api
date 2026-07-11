package xyz.srnyx.annoyingapi.storage.dialects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.storage.FailedSet;
import xyz.srnyx.annoyingapi.storage.CachedValue;

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
    public CachedValue getFromCache(@NotNull String table, @NotNull String target, @NotNull String key) {
        return getFromCacheImpl(table.toLowerCase(), target, key.toLowerCase());
    }

    /**
     * Set a value to the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     * @param   value   the value
     */
    public void setToCache(@NotNull String table, @NotNull String target, @NotNull String key, @Nullable CachedValue value) {
        final String tableLower = table.toLowerCase();
        final String keyLower = key.toLowerCase();
        if (value == null) {
            markRemovedInCache(tableLower, target, keyLower);
            return;
        }
        setToCacheImpl(tableLower, target, keyLower, value);
    }

    /**
     * Mark a value as removed in the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     */
    public void markRemovedInCache(@NotNull String table, @NotNull String target, @NotNull String key) {
        markRemovedInCacheImpl(table.toLowerCase(), target, key.toLowerCase());
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
        saveCacheImpl(table.toLowerCase(), target);
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
        return getFromDatabaseImpl(table.toLowerCase(), target, key.toLowerCase());
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
        return setToDatabaseImpl(table.toLowerCase(), target, key.toLowerCase(), value);
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
    public final List<FailedSet> setToDatabase(@NotNull String table, @NotNull String target, @NotNull Map<String, CachedValue> data) {
        final Map<String, String> dataLower = new LinkedHashMap<>();
        for (final ConcurrentHashMap.Entry<String, CachedValue> entry : data.entrySet()) {
            dataLower.put(entry.getKey().toLowerCase(), entry.getValue().value());
        }
        return setToDatabaseImpl(table.toLowerCase(), target, dataLower);
    }

    /**
     * Set multiple values to the database
     *
     * @param   data    the data to set
     *
     * @return  failed values as {@link FailedSet FailedSets}
     */
    @NotNull
    public final List<FailedSet> setToDatabase(@NotNull ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>>> data) {
        final List<FailedSet> failed = new ArrayList<>();
        for (final Map.Entry<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>>> entry : data.entrySet()) {
            final String table = entry.getKey();
            for (final Map.Entry<String, ConcurrentHashMap<String, CachedValue>> entry1 : entry.getValue().entrySet()) {
                final List<FailedSet> failedList = setToDatabase(table, entry1.getKey(), entry1.getValue());
                if (!failedList.isEmpty()) failed.addAll(failedList);
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
        return removeFromDatabaseImpl(table.toLowerCase(), target, key.toLowerCase());
    }

    /**
     * Get a value from the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     *
     * @return          the value inside a {@link CachedValue}, null if it isn't cached
     */
    @Nullable
    protected abstract CachedValue getFromCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key);

    /**
     * Set a value to the cache
     *
     * @param   table   the table
     * @param   target  the target
     * @param   key     the key
     * @param   value   the value inside a {@link CachedValue}
     */
    protected abstract void setToCacheImpl(@NotNull String table, @NotNull String target, @NotNull String key, @NotNull CachedValue value);

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
     * @return  failed values as {@link FailedSet FailedSets}
     */
    @NotNull
    protected abstract List<FailedSet> setToDatabaseImpl(@NotNull String table, @NotNull String target, @NotNull Map<String, String> data);

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
     * @param tablesKeys    [table, keys]
     * @param data          [table, [target, [key, value]]]
     *
     * @see #getMigrationDataFromDatabase(DataManager)
     */
    public record MigrationData(@NotNull Map<String, Set<String>> tablesKeys, @NotNull ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CachedValue>>> data) {}
}
