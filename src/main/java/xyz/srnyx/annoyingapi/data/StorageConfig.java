package xyz.srnyx.annoyingapi.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.RuntimeLibrary;
import xyz.srnyx.annoyingapi.data.dialects.*;
import xyz.srnyx.annoyingapi.file.AnnoyingFile;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
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
     * The {@link Method storage method}
     */
    @NotNull public final Method method;
    /**
     * The {@link Cache data cache} options
     */
    @NotNull public final Cache cache;
    /**
     * The {@link RemoteConnection remote connection} details/properties
     */
    @Nullable public final RemoteConnection remoteConnection;

    /**
     * Construct a new {@link StorageConfig} instance to parse a storage configuration file
     *
     * @param   file    {@link #file}
     */
    public StorageConfig(@NotNull AnnoyingFile<?> file) {
        this.file = file;
        cache = new Cache();
        final Method getMethod = Method.get(file.getString("method"));

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
            method = Method.H2;
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
        final AnnoyingPlugin plugin = file.plugin;

        // Get url & properties
        String url = method.url.apply(plugin.getDataFolder());
        final Properties properties = new Properties();
        if (remoteConnection != null) {
            url += remoteConnection.host + ":" + remoteConnection.port + "/" + remoteConnection.database;
            if (remoteConnection.username != null) properties.setProperty("user", remoteConnection.username);
            if (remoteConnection.password != null) properties.setProperty("password", remoteConnection.password);
        }

        // Download driver if needed
        if (method.library != null) method.library.load(plugin);

        // Load driver
        try {
            Class.forName(method.getDriver(plugin));
        } catch (final ClassNotFoundException e) {
            throw new ConnectionException(e, url, properties);
        }

        // SQLite: create parent directories
        if (method == Method.SQLITE) {
            final File folder = new File(plugin.getDataFolder(), "data/sqlite");
            if (!folder.exists() && !folder.mkdirs()) throw new ConnectionException("Failed to create SQLite parent directories", url, properties);
        }

        // Connect
        try {
            return DriverManager.getConnection(url, properties);
        } catch (final SQLException e) {
            throw new ConnectionException(e, url, properties);
        }
    }

    /**
     * Options for the {@link DataManager#dataCache}
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
                    .map(SaveOn::fromString)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            saveOn = !providedSaveOns.isEmpty() ? providedSaveOns : new HashSet<>(Arrays.asList(SaveOn.values()));
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
        @NotNull public final String tablePrefix = file.getString("remote-connection.table-prefix", file.plugin.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "_");

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
        }
    }

    /**
     * The driver class for MySQL/MariaDB depending on MySQL Java Connector version
     * <br>1.16.5 uses 8.x.x ({@code com.mysql.cj.jdbc.Driver}), 1.16.4- doesn't ({@code com.mysql.jdbc.Driver})
     */
    @NotNull private static final String MYSQL_MARIADB_DRIVER = AnnoyingPlugin.MINECRAFT_VERSION.isGreaterThanOrEqualTo(1, 16, 5) ? "com{}mysql{}cj{}jdbc{}Driver" : "com{}mysql{}jdbc{}Driver";

    /**
     * The storage method
     */
    public enum Method {
        /**
         * H2 storage method
         */
        H2(H2Dialect::new, "h2{}Driver", dataFolder -> "jdbc:h2:file:.\\" + dataFolder + "\\data\\h2\\data", RuntimeLibrary.H2),
        /**
         * SQLite storage method
         */
        SQLITE(SQLiteDialect::new, "org{}sqlite{}JDBC", dataFolder -> "jdbc:sqlite:" + dataFolder + "\\data\\sqlite\\data.db"),
        /**
         * MySQL storage method
         */
        MYSQL(MySQLDialect::new, MYSQL_MARIADB_DRIVER, dataFolder -> "jdbc:mysql://", 3306),
        /**
         * MariaDB storage method
         */
        MARIADB(MariaDBDialect::new, MYSQL_MARIADB_DRIVER, dataFolder -> "jdbc:mysql://", 3306),
        /**
         * PostgreSQL storage method
         */
        POSTGRESQL(PostgreSQLDialect::new, "postgresql{}Driver", dataFolder -> "jdbc:postgresql://", 5432, RuntimeLibrary.POSTGRESQL);

        /**
         * The {@link SQLDialect SQL dialect} constructor for the method
         */
        @NotNull public final Function<DataManager, SQLDialect> dialect;
        /**
         * The driver class name for the method
         */
        @NotNull private final String driver;
        /**
         * <b>Local:</b> The full URL for the method
         * <br><b>Remote:</b> The beginning of the URL for the method
         */
        @NotNull public final Function<File, String> url;
        /**
         * The default port for the method (only for remote connections)
         */
        @Nullable public final Integer defaultPort;
        /**
         * The library to be downloaded for the method (if any)
         */
        @Nullable public final RuntimeLibrary library;

        /**
         * Construct a new {@link Method} with the given parameters
         *
         * @param   dialect     {@link #dialect}
         * @param   driver      {@link #driver}
         * @param   url         {@link #url}
         * @param   defaultPort {@link #defaultPort}
         * @param   library     {@link #library}
         */
        Method(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull Function<File, String> url, @Nullable Integer defaultPort, @Nullable RuntimeLibrary library) {
            this.dialect = dialect;
            this.driver = driver;
            this.url = url;
            this.defaultPort = defaultPort;
            this.library = library;
        }

        /**
         * Construct a new {@link Method} with the given parameters
         *
         * @param   dialect     {@link #dialect}
         * @param   driver      {@link #driver}
         * @param   url         {@link #url}
         * @param   defaultPort {@link #defaultPort}
         */
        Method(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull Function<File, String> url, int defaultPort) {
            this(dialect, driver, url, defaultPort, null);
        }

        /**
         * Construct a new {@link Method} with the given parameters
         *
         * @param   dialect {@link #dialect}
         * @param   driver  {@link #driver}
         * @param   url     {@link #url}
         * @param   library {@link #library}
         */
        Method(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull Function<File, String> url, @NotNull RuntimeLibrary library) {
            this(dialect, driver, url, null, library);
        }

        /**
         * Construct a new {@link Method} with the given parameters
         *
         * @param   dialect {@link #dialect}
         * @param   driver  {@link #driver}
         * @param   url     {@link #url}
         */
        Method(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull Function<File, String> url) {
            this(dialect, driver, url, null, null);
        }

        /**
         * Get the driver class name for the method
         *
         * @param   plugin  the {@link AnnoyingPlugin plugin} to get the driver for
         *
         * @return          the driver class name for the method
         */
        @NotNull
        public String getDriver(@NotNull AnnoyingPlugin plugin) {
            return (library != null ? plugin.getLibsPackage() + driver : driver).replace("{}", ".");
        }

        /**
         * Whether the method is remote (just checks if {@link #defaultPort} is not {@code null})
         *
         * @return  {@code true} if the method is remote, {@code false} otherwise
         */
        public boolean isRemote() {
            return defaultPort != null;
        }

        /**
         * Get the {@link Method} with the given name
         *
         * @param   name    the name of the method
         *
         * @return          the {@link Method} with the given name, or {@link #H2} if the name is {@code null} or invalid
         */
        @NotNull
        public static Method get(@Nullable String name) {
            if (name == null) return H2;
            try {
                return valueOf(name.toUpperCase());
            } catch (final IllegalArgumentException e) {
                return H2;
            }
        }
    }

    /**
     * Valid values for {@code storage.yml}'s {@code cache.save-on} option
     */
    public enum SaveOn {
        /**
         * Saves the cache on plugin reload
         *
         * @see AnnoyingPlugin#reloadPlugin()
         */
        RELOAD,
        /**
         * Saves the cache on plugin disable
         *
         * @see AnnoyingPlugin#disablePlugin()
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
         * @param string the string to convert
         * @return the converted value, or {@code null} if the string is invalid
         */
        @Nullable
        public static SaveOn fromString(@Nullable String string) {
            if (string == null) return null;
            try {
                return valueOf(string);
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
    }
}
