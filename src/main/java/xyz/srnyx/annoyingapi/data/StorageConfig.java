package xyz.srnyx.annoyingapi.data;

import org.bukkit.configuration.ConfigurationSection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.data.dialects.*;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;


/**
 * The {@code storage.yml} configuration file parser
 */
public class StorageConfig {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * The {@link Method storage method}
     */
    @NotNull public final Method method;
    /**
     * The {@link RemoteConnection remote connection} details/properties
     */
    @Nullable public final RemoteConnection remoteConnection;

    /**
     * Construct a new {@link StorageConfig} instance to parse the {@code storage.yml} configuration file
     *
     * @param   plugin  {@link #plugin}
     */
    public StorageConfig(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
        final AnnoyingResource resource = new AnnoyingResource(plugin, plugin.options.dataOptions.configFile.fileName, plugin.options.dataOptions.configFile.fileOptions);
        final Method getMethod = Method.get(resource.getString("method"));

        // Local storage
        if (!getMethod.isRemote()) {
            method = getMethod;
            remoteConnection = null;
            return;
        }

        // Remote database
        final ConfigurationSection remoteSection = resource.getConfigurationSection("remote-connection");
        if (remoteSection == null) {
            AnnoyingPlugin.log(Level.WARNING, "A remote storage method is used but no remote connection is specified, using H2 instead");
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
     * @throws  ConnectionException if the connection to the database fails for any reason
     */
    @NotNull
    public Connection createConnection() throws ConnectionException {
        // Get url & properties
        String url = method.url.apply(plugin);
        final Properties properties = new Properties();
        if (remoteConnection != null) {
            url += remoteConnection.host + ":" + remoteConnection.port + "/" + remoteConnection.database;
            if (remoteConnection.username != null) properties.setProperty("user", remoteConnection.username);
            if (remoteConnection.password != null) properties.setProperty("password", remoteConnection.password);
        }

        // Load driver
        try {
            Class.forName(method.driver);
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
        @Nullable public final String username;
        /**
         * The remote password
         */
        @Nullable public final String password;
        /**
         * The table prefix for the remote database
         * <br><i>Defaults to the plugin name in lowercase with all non-alphanumeric characters removed + an underscore</i>
         */
        @NotNull public final String tablePrefix;

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

            // username, password, tablePrefix
            username = section.getString("username");
            password = section.getString("password");
            tablePrefix = section.getString("table-prefix", plugin.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "_");
        }
    }

    /**
     * The storage method
     */
    public enum Method {
        /**
         * H2 storage method
         */
        H2(H2Dialect::new, "xyz.srnyx.annoyingapi.libs.h2.Driver", methodPlugin -> "jdbc:h2:file:.\\" + methodPlugin.getDataFolder() + "\\data\\h2\\data"),
        /**
         * SQLite storage method
         */
        SQLITE(SQLiteDialect::new, "org.sqlite.JDBC", methodPlugin -> "jdbc:sqlite:" + methodPlugin.getDataFolder() + "\\data\\sqlite\\data.db"),
        /**
         * MySQL storage method
         */
        MYSQL(MySQLDialect::new, "com.mysql.jdbc.Driver", methodPlugin -> "jdbc:mysql://", 3306),
        /**
         * MariaDB storage method
         */
        MARIADB(MariaDBDialect::new, "com.mysql.jdbc.Driver", methodPlugin -> "jdbc:mysql://", 3306),
        /**
         * PostgreSQL storage method
         */
        POSTGRESQL(PostgreSQLDialect::new, "xyz.srnyx.annoyingapi.libs.postgresql.Driver", methodPlugin -> "jdbc:postgresql://", 5432);

        /**
         * The {@link SQLDialect SQL dialect} constructor for the method
         */
        @NotNull public final Function<DataManager, SQLDialect> dialect;
        /**
         * The driver class name for the method
         */
        @NotNull public final String driver;
        /**
         * <b>Local:</b> The full URL for the method
         * <br><b>Remote:</b> The beginning of the URL for the method
         */
        @NotNull public final Function<AnnoyingPlugin, String> url;
        /**
         * The default port for the method (only for remote connections)
         */
        @Nullable public final Integer defaultPort;

        /**
         * Construct a new {@link Method} with the given parameters
         *
         * @param   dialect     {@link #dialect}
         * @param   driver      {@link #driver}
         * @param   url         {@link #url}
         * @param   defaultPort {@link #defaultPort}
         */
        Method(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull Function<AnnoyingPlugin, String> url, @Nullable Integer defaultPort) {
            this.dialect = dialect;
            this.driver = driver;
            this.url = url;
            this.defaultPort = defaultPort;
        }

        /**
         * Construct a new {@link Method} with the given parameters
         *
         * @param   dialect {@link #dialect}
         * @param   driver  {@link #driver}
         * @param   url     {@link #url}
         */
        Method(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull Function<AnnoyingPlugin, String> url) {
            this(dialect, driver, url, null);
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
}
