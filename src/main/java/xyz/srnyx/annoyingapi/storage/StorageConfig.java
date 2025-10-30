package xyz.srnyx.annoyingapi.storage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * The {@code storage.yml} configuration file parser
 */
public class StorageConfig {
    /**
     * The {@link YamlConfiguration storage configuration file}
     */
    @NotNull public final AnnoyingFile<?> file;
    /**
     * The {@link StorageMethod storage method}
     */
    @NotNull public final StorageMethod method;
    /**
     * The {@link Cache data cache} options
     */
    @NotNull public final Cache cache;
    /**
     * The {@link RemoteConnection remote connection} details/properties
     */
    @Nullable public final RemoteConnection remoteConnection;
    /**
     * Friendly name when migrating between methods for logging
     * <br><b>Format:</b> {@code FILE_PATH (METHOD)}
     */
    @NotNull public final String migrationLogPrefix;

    /**
     * Construct a new {@link StorageConfig} instance to parse a storage configuration file
     *
     * @param   file    {@link #file}
     */
    public StorageConfig(@NotNull AnnoyingFile<?> file) {
        this.file = file;
        cache = new Cache();
        final StorageMethod getMethod = StorageMethod.get(file.getString("method"));
        migrationLogPrefix = "&4" + file.file.getName() + " (" + getMethod + ") &8|&c ";

        // Local storage
        if (!getMethod.isRemote()) {
            method = getMethod;
            remoteConnection = null;
            return;
        }

        // Remote database
        final ConfigurationSection remoteSection = file.getConfigurationSection("remote-connection");
        if (remoteSection == null) {
            AnnoyingPlugin.log(Level.WARNING, file.file.getPath() + " | A remote storage method is used but no remote connection is specified, using H2 instead");
            method = StorageMethod.H2;
            remoteConnection = null;
            return;
        }
        method = getMethod;
        remoteConnection = new RemoteConnection(remoteSection);
    }

    /**
     * Create a new {@link Connection} to the configured database
     *
     * @return                      a new {@link Connection} to the database
     *
     * @throws ConnectionException if the connection to the database fails for any reason
     */
    @NotNull
    public Connection createConnection() throws ConnectionException {
        if (method.url == null) throw new IllegalStateException("The storage method " + method + " is not an SQL method");
        final AnnoyingPlugin plugin = file.plugin;
        final Path dataPath = plugin.getDataFolder().toPath();

        // Get url & properties
        String url = method.url.apply(dataPath);
        final Properties properties = new Properties();
        if (remoteConnection != null) {
            url += remoteConnection.host + ":" + remoteConnection.port + "/" + remoteConnection.database;
            properties.putAll(remoteConnection.properties);
            if (remoteConnection.username != null) properties.setProperty("user", remoteConnection.username);
            if (remoteConnection.password != null) properties.setProperty("password", remoteConnection.password);
        }

        // Get driver
        final Optional<String> driver = method.getDriver();
        if (!driver.isPresent()) throw new ConnectionException("Failed to get driver for " + method, url, properties);

        // SQLite: create parent directories
        if (method == StorageMethod.SQLITE) {
            final File folder = dataPath.resolve("data").resolve("sqlite").toFile();
            if (!folder.exists() && !folder.mkdirs()) throw new ConnectionException("Failed to create SQLite parent directories", url, properties);
        }

        // If downloading library, connect using an IsolatedClassLoader
        if (method.library != null) try {
            final Class<?> driverClass = plugin.libraryManager.loadLibraryIsolated(method.library).loadClass(driver.get());
            return (Connection) driverClass.getMethod("connect", String.class, Properties.class).invoke(driverClass.newInstance(), url, properties);
        } catch (final Exception e) {
            throw new ConnectionException(e, url, properties);
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
    public class RemoteConnection {
        /**
         * The remote host
         */
        @NotNull public final String host;
        /**
         * The remote port
         */
        public final int port;
        /**
         * The remote database name
         */
        @NotNull public final String database;
        /**
         * The remote username
         */
        @Nullable public final String username = file.getString("remote-connection.username");
        /**
         * The remote password
         */
        @Nullable public final String password = file.getString("remote-connection.password");
        /**
         * The table prefix for the remote database
         * <br><i>Defaults to the plugin name in lowercase with all non-alphanumeric characters removed + an underscore</i>
         */
        @NotNull public final String tablePrefix = file.getString("remote-connection.table-prefix", file.plugin.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + "_");
        /**
         * Additional custom properties for the remote connection
         */
        @NotNull public final Map<String, String> properties = new HashMap<>();

        /**
         * Construct a new {@link RemoteConnection} instance to parse the {@code remote-connection} section
         *
         * @param   section the {@link ConfigurationSection remote-connection} section
         */
        public RemoteConnection(@NotNull ConfigurationSection section) {
            // host
            final String getHost = section.getString("host");
            if (getHost == null) throw new IllegalArgumentException("A remote storage method is used but no remote host is specified");
            host = getHost;

            // port
            Integer getPort = section.getInt("port");
            if (getPort == 0) getPort = method.defaultPort;
            if (getPort == null) throw new IllegalArgumentException("A remote storage method is used but no remote port is specified");
            port = getPort;

            // database
            final String getDatabase = section.getString("database");
            if (getDatabase == null) throw new IllegalArgumentException("A remote storage method is used but no remote database is specified");
            database = getDatabase;

            // properties
            final ConfigurationSection propertiesSection = section.getConfigurationSection("properties");
            if (propertiesSection != null) properties.putAll(propertiesSection.getValues(false).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString())));
        }
    }

    /**
     * Options for the data cache (stored differently per method)
     */
    public class Cache {
        /**
         * Whether the cache is enabled
         */
        public final boolean enabled = file.getBoolean("cache.enabled");
        /**
         * When to save the cache
         */
        @NotNull public final Set<SaveOn> saveOn;
        /**
         * The interval to save the cache (if {@link #saveOn} contains {@link SaveOn#INTERVAL})
         */
        public final long interval = file.getLong("cache.interval");

        /**
         * Construct a new {@link Cache} instance to parse the {@code cache} section
         */
        public Cache() {
            final Set<SaveOn> providedSaveOns = file.getStringList("cache.save-on").stream()
                    .map(string -> SaveOn.fromString(string).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            saveOn = !providedSaveOns.isEmpty() ? providedSaveOns : new HashSet<>(Arrays.asList(SaveOn.values()));
        }
    }

    /**
     * Valid values for {@code storage.yml}'s {@code cache.save-on} option
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

        /**
         * Converts the specified string to a {@link SaveOn} value
         *
         * @param   string  the string to convert
         *
         * @return          the converted value, or empty if the string is invalid
         */
        @NotNull
        public static Optional<SaveOn> fromString(@Nullable String string) {
            if (string == null) return Optional.empty();
            try {
                return Optional.of(valueOf(string.toUpperCase()));
            } catch (final IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }
}
