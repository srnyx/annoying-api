package xyz.srnyx.annoyingapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.scheduler.TaskWrapper;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;
import xyz.srnyx.annoyingapi.storage.dialects.sql.SQLDialect;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.logging.Level;


/**
 * The data manager for the plugin, used to manage the connection and data storage
 */
public class DataManager {
    /**
     * The {@link AnnoyingPlugin plugin} to use for the data manager
     */
    @NotNull public final AnnoyingPlugin plugin;
    /**
     * The {@link StorageConfig storage.yml config} for the plugin
     */
    @NotNull public final StorageConfig storageConfig;
    /**
     * The {@link Dialect dialect} for the database
     */
    @NotNull public final Dialect dialect;
    /**
     * The table prefix for the database (only for remote connections)
     */
    @NotNull public final String tablePrefix;
    /**
     * The task that saves the cache on an interval
     *
     * @see DataManager#toggleIntervalCacheSaving()
     */
    @Nullable public TaskWrapper cacheSavingTask;

    /**
     * Connect to the configured database and create the pre-defined tables/columns
     *
     * @param   config              the {@link StorageConfig storage.yml config} to use for the data manager
     *
     * @throws  ConnectionException if the connection to the database fails for any reason
     */
    public DataManager(@NotNull StorageConfig config) throws ConnectionException {
        plugin = config.plugin;
        storageConfig = config;
        dialect = storageConfig.method.dialect.apply(this);
        tablePrefix = storageConfig.remote_connection != null ? storageConfig.remote_connection.table_prefix : "";
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
     * If saving the cache on an interval is enabled, this will start the asynchronous task to do that
     * <br>If the feature is disabled, this will cancel the task if it exists
     */
    public void toggleIntervalCacheSaving() {
        // Cancel ongoing task if one exists
        if (cacheSavingTask != null) cacheSavingTask.cancel();

        // Disable
        if (!storageConfig.cache.getSaveOn().contains(StorageConfig.Cache.SaveOn.INTERVAL)) {
            cacheSavingTask = null;
            return;
        }

        // Enable
        final long ticks = storageConfig.cache.interval.toMillis() / 50;
        cacheSavingTask = plugin.scheduler.runGlobalTaskTimerAsync(task -> dialect.saveCache(), ticks, ticks);
    }

    /**
     * Attempts to migrate data from {@code storage.yml} to {@code storage-new.yml}
     *
     * @return              the new data manager
     */
    @NotNull
    public DataManager attemptDatabaseMigration() {
        // Check if migration is needed (storage-new.yml exists)
        final File dataFolder = plugin.getDataFolder();
        final File storageNew = new File(dataFolder, "storage-new.yml");
        if (!storageNew.exists()) return this;

        // NEW: Load storage-new.yml
        final StorageConfig storageNewConfig = plugin.newStorageConfig(builder -> builder
                .file(storageNew)
                .saveDefaults(false));
        if (storageNewConfig == null) return this;
        AnnoyingPlugin.log(Level.WARNING, "&aSuccessfully found &2storage-new.yml&a, attempting to migrate data from &2" + storageConfig.method + "&a to &2" + storageNewConfig.method + "&a...");

        // NEW: Connect to new database
        final DataManager newManager;
        try {
            newManager = new DataManager(storageNewConfig);
        } catch (final ConnectionException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&4storage-new.yml &8|&c Failed to connect to database! URL: '&4" + e.url + "&c' Properties: &4" + e.getPropertiesRedacted(), e);
            return this;
        }

        // OLD: Get migration data
        final Dialect.MigrationData migrationData = dialect.getMigrationDataFromDatabase(newManager).orElse(null);
        if (migrationData == null) return this;

        if (!migrationData.data().isEmpty()) {
            // NEW: Create missing tables/columns
            if (newManager.dialect instanceof SQLDialect) ((SQLDialect) newManager.dialect).createTablesKeys(migrationData.tablesKeys());
            // NEW: Save values to new database (log failures)
            for (final FailedSet failure : newManager.dialect.setToDatabase(migrationData.data())) AnnoyingPlugin.log(Level.SEVERE, storageConfig.getMigrationLogPrefix() + "Failed to set &4" + failure.column() + "&c for &4" + failure.target() + "&c in table &4" + failure.table() + "&c to &4" + failure.value(), failure.exception());
        } else {
            AnnoyingPlugin.log(Level.SEVERE, storageConfig.getMigrationLogPrefix() + "Found no data to migrate! This may or may not be an error...");
        }

        // OLD: Close old connection
        if (dialect instanceof SQLDialect) try {
            ((SQLDialect) dialect).connection.close();
        } catch (final SQLException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to close the old database connection, it's recommended to restart the server!", e);
        }

        // PREV: Delete storage-old.yml if it exists (from a previous migration)
        final File storageOld = new File(dataFolder, "storage-old.yml");
        if (storageOld.exists()) try {
            Files.delete(storageOld.toPath());
        } catch (final IOException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to delete previous &4storage-old.yml!");
        }

        // Rename files
        final File storage = storageConfig.getBindFile().toFile();
        if (storage.renameTo(storageOld)) { // OLD: storage.yml -> storage-old.yml
            if (storageNew.renameTo(storage)) { // NEW: storage-new.yml -> storage.yml
                // Update OkaeriConfig bind file for StorageConfig
                newManager.storageConfig.configure(configure -> configure.bindFile(storage));
            } else {
                AnnoyingPlugin.log(Level.SEVERE, "\n----------------------------------------\n&cFailed to rename &4storage-new.yml&c to &4storage.yml&c!\nYou MUST rename &4storage-new.yml&c to &4storage.yml&c manually!\n(stop the server first)\n----------------------------------------");
            }
        } else {
            AnnoyingPlugin.log(Level.SEVERE, "\n----------------------------------------\n&cFailed to rename &4storage.yml&c to &4storage-old.yml&c!\nYou MUST rename &4storage.yml&c to &4storage-old.yml&c and &4storage-new.yml&c to &4storage.yml&c manually!\n(stop the server first)\n----------------------------------------");
        }

        // NEW: Use new storage
        AnnoyingPlugin.log(Level.WARNING, "&aSuccessfully finished migrating data from &2" + storageConfig.method + "&a to &2" + storageNewConfig.method + "&a!");
        return newManager;
    }
}
