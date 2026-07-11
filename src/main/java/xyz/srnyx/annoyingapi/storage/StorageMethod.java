package xyz.srnyx.annoyingapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SQLDialect;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.library.AnnoyingAPILibrary;
import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;
import xyz.srnyx.annoyingapi.storage.dialects.Dialect;
import xyz.srnyx.annoyingapi.storage.dialects.JSONDialect;
import xyz.srnyx.annoyingapi.storage.dialects.YAMLDialect;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Available storage methods for the plugin
 */
public enum StorageMethod {
    H2(builder -> builder
            .dialect(() -> SQLDialect.H2)
            .sqlInfo(sqlInfo -> sqlInfo
                    .driver("org{}h2{}Driver")
                    .url(pluginFolder -> "jdbc:h2:file:" + processPath(pluginFolder.resolve("data").resolve("h2").resolve("data")))
                    .library(AnnoyingAPILibrary.H2))),

    SQLITE(builder -> builder
            .dialect(() -> SQLDialect.SQLITE)
            .sqlInfo(sqlInfo -> sqlInfo
                    .driver("org{}sqlite{}JDBC")
                    .url(pluginFolder -> "jdbc:sqlite:" + processPath(pluginFolder.resolve("data").resolve("sqlite").resolve("data.db"))))),

    MYSQL(builder -> builder
            .dialect(() -> SQLDialect.MYSQL)
            .sqlInfo(sqlInfo -> sqlInfo
                    .driver(getMysqlMariadbDriver())
                    .url("jdbc:mysql://")
                    .defaultPort(3306))),

    MARIADB(builder -> builder
            .dialect(() -> SQLDialect.MARIADB)
            .sqlInfo(sqlInfo -> sqlInfo
                    .driver(getMysqlMariadbDriver())
                    .url("jdbc:mysql://")
                    .defaultPort(3306))),

    POSTGRESQL(builder -> builder
            .dialect(() -> SQLDialect.POSTGRES)
            .sqlInfo(sqlInfo -> sqlInfo
                    .driver("org{}postgresql{}Driver")
                    .url("jdbc:postgresql://")
                    .defaultPort(5432)
                    .library(AnnoyingAPILibrary.POSTGRESQL))),

    JSON(builder -> builder.dialect(JSONDialect::new)),

    YAML(builder -> builder.dialect(YAMLDialect::new));

    /**
     * The {@link Dialect} constructor for the method
     */
    @NotNull public final DialectFunction dialect;
    @Nullable public final SQLInfo sqlInfo;

    StorageMethod(@NotNull Consumer<Builder> builder) {
        final Builder b = new Builder();
        builder.accept(b);
        if (b.dialect == null) throw new IllegalStateException("dialect cannot be null");
        this.dialect = b.dialect;
        this.sqlInfo = b.sqlInfo == null ? null : b.sqlInfo.build();
    }

    /**
     * Whether the method is SQL (just checks if {@link #sqlInfo} is not {@code null})
     *
     * @return  {@code true} if the method is SQL, {@code false} otherwise
     */
    public boolean isSQL() {
        return sqlInfo != null;
    }

    /**
     * Whether the method is SQL and remote (just checks if {@link SQLInfo#defaultPort()} is not {@code null})
     *
     * @return {@code true} if the method is SQL and remote, {@code false} otherwise
     */
    public boolean isSQLRemote() {
        return isSQL() && sqlInfo.defaultPort != null;
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
     * Process a {@link Path} to a standardized string format for database URLs
     *
     * @param   path    the {@link Path} to process
     *
     * @return          the processed path as a string
     */
    @NotNull
    private static String processPath(@NotNull Path path) {
        return path.toAbsolutePath().toString().replace(File.separatorChar, '/');
    }

    /**
     * A function to create a new {@link Dialect}
     */
    @FunctionalInterface
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

    /**
     * @param   url <b>Local:</b> The full URL for the method
     *              <br><b>Remote:</b> The beginning of the URL for the method
     */
    public record SQLInfo(@NotNull String driver, @NotNull Function<Path, String> url, @Nullable AnnoyingLibrary library, @Nullable Integer defaultPort) {
        private static class Builder {
            @Nullable public String driver;
            @Nullable public Function<Path, String> url;
            @Nullable public AnnoyingLibrary library;
            @Nullable public Integer defaultPort;

            @NotNull
            public Builder driver(@NotNull String driver) {
                this.driver = AnnoyingPlugin.replaceBrackets(driver);
                return this;
            }

            @NotNull
            public Builder url(@NotNull Function<Path, String> url) {
                this.url = url;
                return this;
            }

            @NotNull
            public Builder url(@NotNull String url) {
                return url(file -> url);
            }

            @NotNull
            public Builder library(@NotNull AnnoyingLibrary library) {
                this.library = library;
                return this;
            }

            @NotNull
            public Builder defaultPort(int defaultPort) {
                this.defaultPort = defaultPort;
                return this;
            }

            @NotNull
            public SQLInfo build() {
                if (driver == null) throw new NullPointerException("driver cannot be null");
                if (url == null) throw new NullPointerException("url cannot be null");
                return new SQLInfo(driver, url, library, defaultPort);
            }
        }
    }

    private static class Builder {
        @Nullable public DialectFunction dialect;
        @Nullable public SQLInfo.Builder sqlInfo;

        @NotNull
        public Builder dialect(@NotNull DialectFunction dialect) {
            this.dialect = dialect;
            return this;
        }

        @NotNull
        public Builder dialect(@NotNull Supplier<?> dialect) {
            return dialect(manager -> {
                // Load jOOQ library
                if (manager.plugin.libraryManager != null && !manager.plugin.libraryManager.loadLibrary(AnnoyingAPILibrary.JOOQ)) {
                    throw new IllegalStateException("Failed to download jOOQ library for " + manager.storageConfig.method);
                }

                // Create dialect
                return new xyz.srnyx.annoyingapi.storage.dialects.SQLDialect(manager, (SQLDialect) dialect.get());
            });
        }

        @NotNull
        public Builder sqlInfo(@NotNull Consumer<SQLInfo.Builder> sqlInfo) {
            this.sqlInfo = new SQLInfo.Builder();
            sqlInfo.accept(this.sqlInfo);
            return this;
        }
    }
}
