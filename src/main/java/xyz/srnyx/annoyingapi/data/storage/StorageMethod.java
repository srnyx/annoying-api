package xyz.srnyx.annoyingapi.data.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.RuntimeLibrary;
import xyz.srnyx.annoyingapi.data.storage.dialects.Dialect;
import xyz.srnyx.annoyingapi.data.storage.dialects.JSONDialect;
import xyz.srnyx.annoyingapi.data.storage.dialects.YAMLDialect;
import xyz.srnyx.annoyingapi.data.storage.dialects.sql.*;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;


/**
 * Available storage methods for the plugin
 */
public enum StorageMethod {
    /**
     * H2 storage method
     */
    H2(H2Dialect::new, "org{}h2{}Driver", dataFolder -> "jdbc:h2:file:.\\" + dataFolder + "\\data\\h2\\data", RuntimeLibrary.H2),
    /**
     * SQLite storage method
     */
    SQLITE(SQLiteDialect::new, "org{}sqlite{}JDBC", dataFolder -> "jdbc:sqlite:" + dataFolder + "\\data\\sqlite\\data.db", null),
    /**
     * MySQL storage method
     */
    MYSQL(MySQLDialect::new, getMysqlMariadbDriver(), "jdbc:mysql://", null, 3306),
    /**
     * MariaDB storage method
     */
    MARIADB(MariaDBDialect::new, getMysqlMariadbDriver(), "jdbc:mysql://", null, 3306),
    /**
     * PostgreSQL storage method
     */
    POSTGRESQL(PostgreSQLDialect::new, "org{}postgresql{}Driver", "jdbc:postgresql://", RuntimeLibrary.POSTGRESQL, 5432),
    /**
     * JSON storage method
     */
    JSON(JSONDialect::new),
    /**
     * YAML storage method
     */
    YAML(YAMLDialect::new);

    /**
     * The {@link Dialect} constructor for the method
     */
    @NotNull public final DialectFunction dialect;
    /**
     * The driver class name for the method. {@code null} if the method is not SQL
     */
    @Nullable private final String driver;
    /**
     * <b>Local SQL:</b> The full URL for the method
     * <br><b>Remote SQL:</b> The beginning of the URL for the method
     * <br><b>Local Readable:</b> {@code null}
     */
    @Nullable public final Function<File, String> url;
    /**
     * The library to be downloaded for the method (if any)
     */
    @Nullable public final RuntimeLibrary library;
    /**
     * The default port for the method (only for remote connections)
     */
    @Nullable public final Integer defaultPort;

    /**
     * Construct a new {@link StorageMethod} with the given parameters (non-remote types)
     *
     * @param dialect     {@link #dialect}
     * @param driver      {@link #driver}
     * @param url         {@link #url}
     * @param library     {@link #library}
     */
    StorageMethod(@NotNull DialectFunction dialect, @Nullable String driver, @Nullable Function<File, String> url, @Nullable RuntimeLibrary library) {
        this.dialect = dialect;
        this.driver = driver;
        this.url = url;
        this.library = library;
        this.defaultPort = null;
    }

    /**
     * Construct a new {@link StorageMethod} with the given parameters (remote types)
     *
     * @param dialect     {@link #dialect}
     * @param driver      {@link #driver}
     * @param url         {@link #url}
     * @param library     {@link #library}
     * @param defaultPort {@link #defaultPort}
     */
    StorageMethod(@NotNull DialectFunction dialect, @NotNull String driver, @NotNull String url, @Nullable RuntimeLibrary library, @Nullable Integer defaultPort) {
        this.dialect = dialect;
        this.driver = driver;
        this.url = file -> url;
        this.library = library;
        this.defaultPort = defaultPort;
    }

    StorageMethod(@NotNull DialectFunction dialect) {
        this.dialect = dialect;
        this.driver = null;
        this.url = null;
        this.library = null;
        this.defaultPort = null;
    }

    /**
     * Get the driver class name for the method
     *
     * @return  the driver class name for the method
     */
    @NotNull
    public Optional<String> getDriver() {
        return driver != null ? Optional.of(driver.replace("{}", ".")) : Optional.empty();
    }

    /**
     * Whether the method is remote (just checks if {@link #defaultPort} is not {@code null})
     *
     * @return {@code true} if the method is remote, {@code false} otherwise
     */
    public boolean isRemote() {
        return defaultPort != null;
    }

    /**
     * Whether the method is SQL (just checks if {@link #driver} is not {@code null})
     *
     * @return  {@code true} if the method is SQL, {@code false} otherwise
     */
    public boolean isSQL() {
        return driver != null;
    }

    /**
     * Get the {@link StorageMethod} with the given name
     *
     * @param name the name of the method
     * @return the {@link StorageMethod} with the given name, or {@link #H2} if the name is {@code null} or invalid
     */
    @NotNull
    public static StorageMethod get(@Nullable String name) {
        if (name != null) try {
            return valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException ignored) {}
        return H2;
    }

    /**
     * The driver class for MySQL/MariaDB depending on MySQL Java Connector version
     * <br>1.16.5 uses 8.x.x ({@code com.mysql.cj.jdbc.Driver}), 1.16.4- doesn't ({@code com.mysql.jdbc.Driver})
     *
     * @return  the driver class for MySQL/MariaDB
     */
    @NotNull
    private static String getMysqlMariadbDriver() {
        return AnnoyingPlugin.MINECRAFT_VERSION.isGreaterThanOrEqualTo(1, 16, 5) ? "com{}mysql{}cj{}jdbc{}Driver" : "com{}mysql{}jdbc{}Driver";
    }

    /**
     * A function to create a new {@link Dialect}
     */
    public interface DialectFunction {
        /**
         * Apply the function to create a new {@link Dialect}
         *
         * @param   dataManager         the {@link DataManager} to use for the dialect
         *
         * @return                      the new {@link Dialect}
         *
         * @throws  ConnectionException if the connection to the database fails for any reason (SQL only)
         */
        @NotNull
        Dialect apply(@NotNull DataManager dataManager) throws ConnectionException;
    }
}
