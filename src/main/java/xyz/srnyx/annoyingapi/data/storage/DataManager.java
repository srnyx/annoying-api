package xyz.srnyx.annoyingapi.data.storage;

import org.bukkit.Bukkit;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.storage.dialects.Dialect;
import xyz.srnyx.annoyingapi.data.storage.dialects.sql.SQLDialect;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Map;
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
     * Connect to the configured database and create the pre-defined tables/columns
     *
     * @param   file                the {@link AnnoyingFile file} to get the connection information from
     *
     * @throws ConnectionException if the connection to the database fails for any reason
     */
    public DataManager(@NotNull AnnoyingFile<?> file) throws ConnectionException {
        plugin = file.plugin;
        storageConfig = new StorageConfig(file);
        dialect = storageConfig.method.dialect.apply(this);
        tablePrefix = storageConfig.remoteConnection != null ? storageConfig.remoteConnection.tablePrefix : "";
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
     * Starts the asynchronous task to save the cache on an interval
     */
    public void startCacheSavingOnInterval() {
        final long interval = storageConfig.cache.interval;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, dialect::saveCache, interval, interval);
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
        AnnoyingPlugin.log(Level.INFO, "&aFound &2storage-new.yml&a, attempting to migrate data from &2storage.yml&a to &2storage-new.yml&a...");

        // NEW: Connect to new database
        final AnnoyingFile<?> storageNewFile = new AnnoyingFile<>(plugin, storageNew, new AnnoyingFile.Options<>().canBeEmpty(false));
        if (!storageNewFile.load()) return this;
        final DataManager newManager;
        try {
            newManager = new DataManager(storageNewFile);
        } catch (final ConnectionException e) {
            AnnoyingPlugin.log(Level.SEVERE, "&4storage-new.yml &8|&c Failed to connect to database! URL: '&4" + e.url + "&c' Properties: &4" + e.getPropertiesRedacted(), e);
            return this;
        }

        // OLD: Get migration data
        final Dialect.MigrationData migrationData = dialect.getMigrationDataFromDatabase(newManager).orElse(null);
        if (migrationData == null) return this;

        if (!migrationData.data.isEmpty()) {
            // NEW: Create missing tables/columns
            if (newManager.dialect instanceof SQLDialect) ((SQLDialect) newManager.dialect).createTablesKeys(migrationData.tablesKeys);
            // NEW: Save values to new database (log failures)
            for (final Map.Entry<String, Map<String, Map<String, String>>> entry : newManager.dialect.setToDatabase(migrationData.data).entrySet()) {
                final String table = entry.getKey();
                for (final Map.Entry<String, Map<String, String>> entry1 : entry.getValue().entrySet()) AnnoyingPlugin.log(Level.SEVERE, storageConfig.migrationLogPrefix + "Failed to set values for &4" + entry1.getKey() + "&c in table &4" + table + "&c: &4" + entry1.getValue());
            }
        } else {
            AnnoyingPlugin.log(Level.WARNING, storageConfig.migrationLogPrefix + "Found no data to migrate! This may or may not be an error...");
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
        final File storage = storageConfig.file.file;
        if (storage.renameTo(storageOld)) { // OLD: storage.yml -> storage-old.yml
            if (!storageNew.renameTo(storage)) { // NEW: storage-new.yml -> storage.yml
                AnnoyingPlugin.log(Level.SEVERE, "&cFailed to rename &4storage-new.yml&c to &4storage.yml&c! You MUST rename &4storage-new.yml&c to &4storage.yml&c manually!");
            }
        } else {
            AnnoyingPlugin.log(Level.SEVERE, "&cFailed to rename &4storage.yml&c to &4storage-old.yml&c! You MUST rename &4storage.yml&c to &4storage-old.yml&c and &4storage-new.yml&c to &4storage.yml&c manually!");
        }

        // NEW: Use new storage
        AnnoyingPlugin.log(Level.INFO, "&aFinished migrating data from &2storage.yml&a to &2storage-new.yml&a!");
        return newManager;
    }
}
