package xyz.srnyx.annoyingapi.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.commons.lang.time.DurationFormatUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * General utility methods for Annoying API
 */
public class AnnoyingUtility {
    @NotNull private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)&[0-9A-FK-OR]");

    /**
     * Translates {@code &} color codes to {@link ChatColor}
     *
     * @param   object  the object/message to translate
     *
     * @return          the translated object/message
     */
    @NotNull
    public static String color(@Nullable Object object) {
        if (object == null) return "null";
        return ChatColor.translateAlternateColorCodes('&', String.valueOf(object));
    }

    /**
     * Translates {@code &} color codes to {@link ChatColor} for a {@link Collection} of messages
     *
     * @param   objects the objects/messages to translate
     *
     * @return          the translated objects/messages
     */
    @NotNull
    public static List<String> colorCollection(@Nullable Collection<?> objects) {
        if (objects == null) return new ArrayList<>();
        return objects.stream()
                .map(AnnoyingUtility::color)
                .collect(Collectors.toList());
    }

    /**
     * Strips untranslated {@link ChatColor ChatColors} (using {@code &}) from a {@link String}
     *
     * @param   string  the {@link String} to strip
     *
     * @return          the stripped {@link String}
     */
    @NotNull
    public static String stripUntranslatedColor(@Nullable String string) {
        if (string == null) return "null";
        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }

    /**
     * Formats a millisecond long using the given pattern
     *
     * @param   value           the milliseconds to format
     * @param   pattern         the way in which to format the milliseconds
     * @param   padWithZeros    whether to pad the left hand side of numbers with 0's
     *
     * @return                  the formatted milliseconds
     */
    @NotNull
    public static String formatMillis(long value, @Nullable String pattern, boolean padWithZeros) {
        if (pattern == null) pattern = "m':'s";
        return DurationFormatUtils.formatDuration(value, pattern, padWithZeros);
    }

    /**
     * Formats a {@link Double} value using the given pattern
     *
     * @param   value   the {@link Number} to format
     * @param   pattern the pattern to use
     *
     * @return          the formatted value
     */
    @NotNull
    public static String formatNumber(@NotNull Number value, @Nullable String pattern) {
        if (pattern == null) pattern = "#,###.##";
        return new DecimalFormat(pattern).format(value);
    }

    /**
     * Gets an {@link OfflinePlayer} from the specified name
     *
     * @param   name    the name of the player
     *
     * @return          the {@link OfflinePlayer}, or null if not found
     */
    @Nullable
    public static OfflinePlayer getOfflinePlayer(@NotNull String name) {
        for (final OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            final String offlineName = offline.getName();
            if (offlineName != null && offlineName.equalsIgnoreCase(name)) return offline;
        }
        return null;
    }

    /**
     * Gets a {@link Set} of all online player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all offline player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getOfflinePlayerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> !player.isOnline())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all player names
     *
     * @return  the {@link Set} of player names
     */
    @NotNull
    public static Set<String> getAllPlayerNames() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all world names
     *
     * @return  the {@link Set} of world names
     */
    @NotNull
    public static Set<String> getWorldNames() {
        return Bukkit.getWorlds().stream()
                .map(org.bukkit.World::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all the enum's value's names
     *
     * @param   enumClass   the enum class to get the names from
     *
     * @return              the {@link Set} of the enum's value's names
     */
    @NotNull
    public static Set<String> getEnumNames(@NotNull Class<? extends Enum<?>> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    /**
     * Gets a {@link Set} of all YML file names in a folder. If the path is not a folder, an empty {@link Set} is returned
     *
     * @param   plugin  the {@link AnnoyingPlugin} to get the folder from
     * @param   path    the path to the folder
     *
     * @return  {@link Set} all YML file names in the folder
     */
    @NotNull
    public static Set<String> getFileNames(@NotNull AnnoyingPlugin plugin, @NotNull String path) {
        final File[] files = new File(plugin.getDataFolder(), path).listFiles();
        if (files == null) return new HashSet<>();
        return Arrays.stream(files)
                .map(File::getName)
                .filter(name -> name.endsWith(".yml"))
                .map(name -> name.substring(0, name.length() - 4))
                .collect(Collectors.toSet());
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
    public static JsonElement getJson(@NotNull String userAgent, @NotNull String urlString) {
        final HttpURLConnection connection;
        final JsonElement json;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", userAgent);
            if (connection.getResponseCode() == 404) return null;
            json = new JsonParser().parse(new InputStreamReader(connection.getInputStream()));
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
        connection.disconnect();
        return json;
    }

    /**
     * Constructs a new {@link AnnoyingUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private AnnoyingUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
