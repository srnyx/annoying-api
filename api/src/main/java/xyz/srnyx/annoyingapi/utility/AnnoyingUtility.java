package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * General utility methods for Annoying API
 */
public class AnnoyingUtility {
    /**
     * Repeats a {@link CharSequence} a given amount of times
     *
     * @param   charSequence    the {@link CharSequence} to repeat
     * @param   amount          the amount of times to repeat the {@link CharSequence}
     *
     * @return                  the repeated {@link CharSequence}
     */
    @NotNull
    public static String repeat(@NotNull CharSequence charSequence, int amount) {
        return String.join("", Collections.nCopies(amount, charSequence));
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
     * Constructs a new {@link AnnoyingUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private AnnoyingUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
