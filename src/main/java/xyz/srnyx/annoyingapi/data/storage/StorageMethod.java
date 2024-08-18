package xyz.srnyx.annoyingapi.data.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.RuntimeLibrary;
import xyz.srnyx.annoyingapi.data.storage.dialects.*;

import java.io.File;
import java.util.function.Function;


/**
 * The storage method
 */
public enum StorageMethod {
    /**
     * H2's storage method
     */
    H2(H2Dialect::new, "h2{}Driver", dataFolder -> "jdbc:h2:file:.\\" + dataFolder + "\\data\\h2\\data", null, RuntimeLibrary.H2),
    /**
     * SQLite's storage method
     */
    SQLITE(SQLiteDialect::new, "org{}sqlite{}JDBC", dataFolder -> "jdbc:sqlite:" + dataFolder + "\\data\\sqlite\\data.db", null, null),
    /**
     * MySQL's storage method
     */
    MYSQL(MySQLDialect::new, getMysqlMariadbDriver(), "jdbc:mysql://", 3306, null),
    /**
     * MariaDB's storage method
     */
    MARIADB(MariaDBDialect::new, getMysqlMariadbDriver(), "jdbc:mysql://", 3306, null),
    /**
     * PostgreSQL's storage method
     */
    POSTGRESQL(PostgreSQLDialect::new, "postgresql{}Driver", "jdbc:postgresql://", 5432, RuntimeLibrary.POSTGRESQL);

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
     * Construct a new {@link StorageMethod} with the given parameters
     *
     * @param dialect     {@link #dialect}
     * @param driver      {@link #driver}
     * @param url         {@link #url}
     * @param defaultPort {@link #defaultPort}
     * @param library     {@link #library}
     */
    StorageMethod(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull Function<File, String> url, @Nullable Integer defaultPort, @Nullable RuntimeLibrary library) {
        this.dialect = dialect;
        this.driver = driver;
        this.url = url;
        this.defaultPort = defaultPort;
        this.library = library;
    }

    /**
     * Construct a new {@link StorageMethod} with the given parameters
     *
     * @param dialect     {@link #dialect}
     * @param driver      {@link #driver}
     * @param url         {@link #url}
     * @param defaultPort {@link #defaultPort}
     * @param library     {@link #library}
     */
    StorageMethod(@NotNull Function<DataManager, SQLDialect> dialect, @NotNull String driver, @NotNull String url, @Nullable Integer defaultPort, @Nullable RuntimeLibrary library) {
        this(dialect, driver, dataFolder -> url, defaultPort, library);
    }

    /**
     * Get the driver class name for the method
     *
     * @param plugin the {@link AnnoyingPlugin plugin} to get the driver for
     * @return the driver class name for the method
     */
    @NotNull
    public String getDriver(@NotNull AnnoyingPlugin plugin) {
        return (library != null ? plugin.getLibsPackage() + driver : driver).replace("{}", ".");
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
     * Get the {@link StorageMethod} with the given name
     *
     * @param name the name of the method
     * @return the {@link StorageMethod} with the given name, or {@link #H2} if the name is {@code null} or invalid
     */
    @NotNull
    public static StorageMethod get(@Nullable String name) {
        if (name == null) return H2;
        try {
            return valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException e) {
            return H2;
        }
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
}
