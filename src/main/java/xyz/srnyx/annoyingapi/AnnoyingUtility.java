package xyz.srnyx.annoyingapi;

import org.bukkit.ChatColor;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * General utility methods for AnnoyingAPI
 */
public class AnnoyingUtility {
    /**
     * Translates {@code &} color codes to {@link ChatColor}
     *
     * @param   message the message to translate
     *
     * @return          the translated message
     */
    @NotNull
    @Contract("_ -> new")
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Formats a millisecond long using the given pattern
     *
     * @param   millis  the milliseconds to format
     * @param   pattern the pattern to use
     *
     * @return          the formatted time
     */
    @NotNull
    public static String formatMillis(@NotNull Date millis, @Nullable String pattern) {
        if (pattern == null) pattern = "mm:ss";
        return new SimpleDateFormat(pattern).format(millis);
    }
}
