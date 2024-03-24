package xyz.srnyx.annoyingapi.data;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;


/**
 * Represents an exception that occurs while attempting to connect to a database
 */
public class ConnectionException extends Exception {
    /**
     * The URL of the database connection
     */
    @NotNull public final String url;
    /**
     * The properties used to connect to the database
     */
    @NotNull public final Properties properties;

    /**
     * Constructs a new connection exception with the given URL and properties
     *
     * @param   e           the exception that occurred
     * @param   url         {@link #url}
     * @param   properties  {@link #properties}
     */
    public ConnectionException(@NotNull Throwable e, @NotNull String url, @NotNull Properties properties) {
        super(e);
        this.url = url;
        this.properties = properties;
    }

    public ConnectionException(@NotNull String message, @NotNull String url, @NotNull Properties properties) {
        super(message);
        this.url = url;
        this.properties = properties;
    }
}
