package xyz.srnyx.annoyingapi.storage;

import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.validator.annotation.NotNull;
import eu.okaeri.validator.annotation.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.RootConfig;
import xyz.srnyx.annoyingapi.file.okaeri.SubConfig;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.duration.DurationTickFallback;
import xyz.srnyx.annoyingapi.stats.Stat;
import xyz.srnyx.javautilities.MapGenerator;

import java.time.Duration;
import java.util.*;


@Header("DOCUMENTATION: https://annoying-api.srnyx.com/wiki/data-storage")
@Header("The documentation includes the process for method migration (e.g. H2 -> MYSQL, etc.)")
public class StorageConfig extends RootConfig {
    @Comment
    @Comment
    @Comment("The method that data will be stored. Available options are listed below")
    @Comment("If you want to switch to a different storage method, please see the documentation for a guide on how to migrate your data")
    @Comment(" ")
    @Comment("LOCAL SQL (data will be stored on the Minecraft server in SQL format, connection configuration NOT required)")
    @Comment("These methods are recommended for most non-network servers")
    @Comment("- H2 (default)")
    @Comment("- SQLITE")
    @Comment(" ")
    @Comment("REMOTE SQL (data will be stored on a remote database in SQL format, connection configuration REQUIRED)")
    @Comment("These methods are recommended for network servers (ones with proxies) or servers with multiple instances")
    @Comment("- MYSQL")
    @Comment("- MARIADB")
    @Comment("- POSTGRESQL")
    @Comment(" ")
    @Comment("LOCAL READABLE (data will be stored on the Minecraft server in a human-readable format, connection configuration NOT required)")
    @Comment("These methods are NOT recommended as they impact performance significantly! It's strongly recommended to at least keep the cache enabled")
    @Comment("- JSON")
    @Comment("- YAML")
    @Stat(key = "method")
    @NotNull public StorageMethod method = StorageMethod.H2;

    @Comment
    @Comment("The connection configuration for REMOTE databases")
    @Comment("NOTE: If you are using a LOCAL database, you can ignore this section")
    @NotNull public RemoteConnection remote_connection;

    @Comment
    @Comment("Options for the data cache, which is used for both LOCAL and REMOTE storage methods")
    @Comment("The cache greatly improves performance by storing data in memory")
    @Comment("However, there is a potential risk of data loss if the server crashes before the data is saved to the database")
    @NotNull public Cache cache = new Cache(this);


    @org.jetbrains.annotations.NotNull public transient final AnnoyingPlugin plugin;

    public StorageConfig(@org.jetbrains.annotations.NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
        this.remote_connection = new RemoteConnection(this);
    }

    /**
     * Friendly name when migrating between methods for logging
     * <br><b>Format:</b> {@code FILE_PATH (METHOD)}
     */
    @org.jetbrains.annotations.NotNull
    public String getMigrationLogPrefix() {
        return "&4" + getBindFileName() + " (" + method + ") &8|&c ";
    }

    /**
     * The remote connection details/properties
     */
    public static class RemoteConnection extends SubConfig<StorageConfig, StorageConfig> {
        @Comment("The host of the database")
        @NotNull public String host = "localhost";
        @Comment("The port of the database")
        @Comment("Defaults: 3306 for MySQL/MariaDB, 5432 for PostgreSQL")
        public int port = 3306;

        @Comment
        @Comment("The name of the database")
        @Comment("THE DATABASE MUST ALREADY EXIST")
        @NotNull public String database = "minecraft";

        @Comment
        @Comment("The username and password of the database")
        @NotNull public String username = "";
        @NotNull public String password = "";

        /**
         * The table prefix for the remote database
         * <br><i>Defaults to the plugin name in lowercase with all non-alphanumeric characters removed + an underscore</i>
         */
        @Comment
        @Comment("If you're using one database for multiple plugins, it's recommended to have a table prefix (case-insensitive)")
        @Comment("By default (if null), the table prefix will be the name of the plugin (special characters and spaces removed, all lowercase) followed by an underscore")
        @Comment("To remove the table prefix, set it to an empty string ('')")
        @Comment("DO NOT CHANGE THIS AFTER YOU'VE STARTED USING THE PLUGIN (unless you're migrating or fine with losing data)")
        @Nullable public String table_prefix = getDefaultTablePrefix(getParent().plugin);

        /**
         * Additional custom properties for the remote connection
         */
        @Comment
        @Comment("Additional properties for the connection")
        @Comment("You may need to remove useUnicode and characterEncoding if you're using PostgreSQL")
        @Comment("It's recommended to keep 'autoReconnect: true'")
        @NotNull public Map<String, String> properties = MapGenerator.LINKED_HASH_MAP.mapOf(
                "autoReconnect", "true",
                "useUnicode", "true",
                "characterEncoding", "UTF-8");


        public RemoteConnection(@org.jetbrains.annotations.NotNull StorageConfig root) {
            super(root);
            if (root.method.sqlInfo != null && root.method.sqlInfo.defaultPort() != null) this.port = root.method.sqlInfo.defaultPort();
        }

        @org.jetbrains.annotations.NotNull
        public static String getDefaultTablePrefix(@org.jetbrains.annotations.NotNull AnnoyingPlugin plugin) {
            return plugin.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + "_";
        }
    }

    /**
     * Options for the data cache (stored differently per method)
     */
    public static class Cache extends SubConfig<StorageConfig, StorageConfig> {
        public Cache(@org.jetbrains.annotations.NotNull StorageConfig root) {
            super(root);
        }

        @Comment("Whether to enable using the cache")
        @Stat(key = "enabled")
        public boolean enabled = true;

        /**
         * When to save the cache
         */
        @Comment("The actions that will trigger the cache to save to the database")
        @Comment("If empty, all actions will trigger the cache to save")
        @Comment("Actions:")
        @Comment("- RELOAD (recommended): When the plugin (not the server) is reloaded")
        @Comment("- DISABLE (highly recommended): When the plugin is disabled (including on server shutdown)")
        @Comment("- INTERVAL (recommended): Automatically at a fixed interval (see 'interval' below)")
        @Stat(key = "save_on")
        @NotNull private Set<SaveOn> save_on = SaveOn.VALUES;

        /**
         * The interval to save the cache (if {@link #save_on} contains {@link SaveOn#INTERVAL})
         */
        @Comment("The interval in which the cache will save to the database (only applicable if 'INTERVAL' is in 'save_on')")
        @Comment("Make sure to specify units (s, m, h, etc.)!")
        @DurationTickFallback @Stat(key = "interval")
        @NotNull public Duration interval = Duration.ofMinutes(5);

        @org.jetbrains.annotations.NotNull
        public Set<SaveOn> getSaveOn() {
            return save_on.isEmpty() ? SaveOn.VALUES : save_on;
        }

        /**
         * Valid values for {@link #save_on}
         */
        public enum SaveOn {
            /**
             * Saves the cache on plugin reload
             *
             * @see AnnoyingPlugin#reload()
             */
            RELOAD,
            /**
             * Saves the cache on plugin disable
             *
             * @see AnnoyingPlugin#disable()
             */
            DISABLE,
            /**
             * Saves the cache on an interval
             *
             * @see Cache#interval
             */
            INTERVAL;

            @org.jetbrains.annotations.NotNull private static final Set<SaveOn> VALUES = Collections.unmodifiableSet(EnumSet.allOf(SaveOn.class));
        }
    }
}
