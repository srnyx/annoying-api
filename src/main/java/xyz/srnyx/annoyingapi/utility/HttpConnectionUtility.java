package xyz.srnyx.annoyingapi.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Utility class for making HTTP requests
 */
public class HttpConnectionUtility {
    /**
     * Requests an {@link InputStreamReader} from a URL and returns the result of the specified function
     *
     * @param   userAgent   the user agent to use
     * @param   urlString   the URL to request from
     * @param   function    the function to apply to the {@link InputStreamReader}
     *
     * @param   <T>         the type of the result of the specified function
     *
     * @return              the result of the specified function, or null if the request failed
     */
    @Nullable
    public static <T> T processRequest(@NotNull String userAgent, @NotNull String urlString, Function<InputStreamReader, T> function) {
        final HttpURLConnection connection;
        final T result;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            if (connection.getResponseCode() == 404) return null;
            result = function.apply(new InputStreamReader(connection.getInputStream()));
        } catch (final IOException e) {
            return null;
        }
        connection.disconnect();
        return result;
    }

    /**
     * Requests a {@link String} from a URL
     *
     * @param   userAgent   the user agent to use
     * @param   urlString   the URL to request from
     *
     * @return              the {@link String}, or null if the request failed
     */
    @Nullable
    public static String requestString(@NotNull String userAgent, @NotNull String urlString) {
        return processRequest(userAgent, urlString, reader -> new BufferedReader(reader).lines().collect(Collectors.joining("\n")));
    }

    /**
     * Retrieves the {@link JsonElement} from the specified URL
     *
     * @param   userAgent   the user agent to use when retrieving the {@link JsonElement}
     * @param   urlString   the URL to retrieve the {@link JsonElement} from
     *
     * @return              the {@link JsonElement} retrieved from the specified URL
     */
    @Nullable
    public static JsonElement requestJson(@NotNull String userAgent, @NotNull String urlString) {
        return processRequest(userAgent, urlString, reader -> new JsonParser().parse(reader));
    }

    /**
     * Constructs a new {@link HttpConnectionUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private HttpConnectionUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
