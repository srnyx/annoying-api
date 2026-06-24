package xyz.srnyx.annoyingapi.storage;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.validator.annotation.NotNull;
import eu.okaeri.validator.annotation.Nullable;
import net.byteflux.libby.classloader.IsolatedClassLoader;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.SubConfig;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;


@Header("DOCUMENTATION: https://annoying-api.srnyx.com/wiki/data-storage")
@Header("The documentation includes the process for method migration (e.g. H2 -> MYSQL, etc.)")
public class StorageConfig extends OkaeriConfig {
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
     * Create a new {@link Connection} to the configured database
     *
     * @return  a new {@link Connection} to the database
     *
     * @throws  ConnectionException if the connection to the database fails for any reason
     */
    @org.jetbrains.annotations.NotNull
    public Connection createConnection() throws ConnectionException {
        if (method.url == null) throw new IllegalStateException("The storage method " + method + " is not an SQL method");
        final Path dataPath = plugin.getDataFolder().toPath();

        // Get url & properties
        String url = method.url.apply(dataPath);
        final Properties properties = new Properties();
        if (method.isRemote()) {
            url += remote_connection.host + ":" + remote_connection.getPort() + "/" + remote_connection.database;
            properties.putAll(remote_connection.properties);
            if (!remote_connection.username.isEmpty()) properties.setProperty("user", remote_connection.username);
            if (!remote_connection.password.isEmpty()) properties.setProperty("password", remote_connection.password);
        }

        // Get driver
        final Optional<String> driver = method.getDriver();
        if (driver.isEmpty()) throw new ConnectionException("Failed to get driver for " + method, url, properties);

        // SQLite: create parent directories
        if (method == StorageMethod.SQLITE) {
            final File folder = dataPath.resolve("data").resolve("sqlite").toFile();
            if (!folder.exists() && !folder.mkdirs()) throw new ConnectionException("Failed to create SQLite parent directories", url, properties);
        }

        // If downloading library, connect using an IsolatedClassLoader
        if (method.library != null && plugin.libraryManager != null) {
            // Get IsolatedClassLoader of library
            final IsolatedClassLoader classLoader;
            try {
                classLoader = plugin.libraryManager.loadLibraryIsolated(method.library);
            } catch (final Exception e) {
                throw new ConnectionException(e, url, properties);
            }
            if (classLoader == null) throw new ConnectionException("Failed to load library for " + method, url, properties);

            // Connect using driver from IsolatedClassLoader
            try {
                final Class<?> driverClass = classLoader.loadClass(driver.get());
                return (Connection) driverClass.getMethod("connect", String.class, Properties.class).invoke(driverClass.getDeclaredConstructor().newInstance(), url, properties);
            } catch (final Exception e) {
                throw new ConnectionException(e, url, properties);
            }
        }

        // Load driver and connect
        try {
            Class.forName(driver.get());
            return DriverManager.getConnection(url, properties);
        } catch (final ClassNotFoundException | SQLException e) {
            throw new ConnectionException(e, url, properties);
        }
    }

    /**
     * The remote connection details/properties
     */
    public static class RemoteConnection extends SubConfig<StorageConfig> {
        public RemoteConnection(@org.jetbrains.annotations.NotNull StorageConfig root) {
            super(root);
        }

        @Comment("The host of the database")
        @NotNull public String host = "localhost";
        @Comment("The port of the database")
        @Comment("Defaults: 3306 for MySQL/MariaDB, 5432 for PostgreSQL")
        @Nullable private Integer port = getRoot().method.defaultPort;

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
        @NotNull private String table_prefix = getDefaultTablePrefix();

        /**
         * Additional custom properties for the remote connection
         */
        @Comment
        @Comment("Additional properties for the connection")
        @Comment("You may need to remove useUnicode and characterEncoding if you're using PostgreSQL")
        @Comment("It's recommended to keep 'autoReconnect: true'")
        @NotNull public Map<String, String> properties = Map.of(
                "autoReconnect", "true",
                "useUnicode", "true",
                "characterEncoding", "UTF-8");

        @org.jetbrains.annotations.Nullable
        public Integer getPort() {
            if (port == null) {
                final Integer newPort = getRoot().method.defaultPort;
                if (!Objects.equals(port, newPort)) {
                    port = newPort;
                    save();
                }
            }
            return port;
        }

        @org.jetbrains.annotations.NotNull
        public String getTablePrefix() {
            if (table_prefix == null) {
                final String newTablePrefix = getDefaultTablePrefix();
                if (!Objects.equals(table_prefix, newTablePrefix)) {
                    table_prefix = newTablePrefix;
                    save();
                }
            }
            return table_prefix;
        }


        @org.jetbrains.annotations.NotNull
        public String getDefaultTablePrefix() {
            return getRoot().plugin.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + "_";
        }
    }

    /**
     * Options for the data cache (stored differently per method)
     */
    public static class Cache extends SubConfig<StorageConfig> {
        public Cache(@org.jetbrains.annotations.NotNull StorageConfig root) {
            super(root);
        }

        @Comment("Whether to enable using the cache")
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
        @NotNull private Set<SaveOn> save_on = SaveOn.VALUES;

        /**
         * The interval to save the cache (if {@link #save_on} contains {@link SaveOn#INTERVAL})
         */
        @Comment("The interval in which the cache will save to the database (only applicable if 'INTERVAL' is in 'save_on')")
        @Comment("Make sure to specify units (s, m, h, etc.), else it will default to Minecraft ticks! :)")
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
