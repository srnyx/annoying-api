package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class to format durations. This has the same (or at least similar) functionality as {@link org.apache.commons.lang.time.DurationFormatUtils}
 */
public class DurationFormatUtility {
    /**
     * Pad the left hand side of a String with zeros
     *
     * @param   string  the String to pad out
     * @param   size    the size to pad to
     *
     * @return          the padded String
     */
    @NotNull
    private static String padWithZeros(@NotNull String string, int size) {
        final int pads = size - string.length();
        if (pads <= 0 || pads > 8192) return string;
        return AnnoyingUtility.repeat("0", pads) + string;
    }

    /**
     * Formats the duration as a string, using the specified format
     *
     * @param   durationMillis  the duration to format
     * @param   format          the way in which to format the duration
     * @param   padWithZeros    whether to pad the left hand side of numbers with 0's
     *
     * @return                  the duration as a String
     */
    @NotNull
    public static String formatDuration(long durationMillis, @NotNull String format, boolean padWithZeros) {
        final char[] array = format.toCharArray();
        final List<Token> list = new ArrayList<>(array.length);

        boolean inLiteral = false;
        StringBuilder lexxBuilder = null;
        Token previous = null;
        for (final char ch : array) {
            if (inLiteral && ch != '\'') {
                lexxBuilder.append(ch);
                continue;
            }

            switch (ch) {
                case '\'':
                    if (inLiteral) {
                        lexxBuilder = null;
                        inLiteral = false;
                    } else {
                        lexxBuilder = new StringBuilder();
                        list.add(new Token(lexxBuilder));
                        inLiteral = true;
                    }
                    break;
                case 'y':
                case 'M':
                case 'd':
                case 'H':
                case 'm':
                case 's':
                case 'S':
                    final String value = String.valueOf(ch);
                    if (previous != null && previous.value.toString().equals(value)) {
                        previous.count++;
                    } else {
                        final Token token = new Token(new StringBuilder(value));
                        list.add(token);
                        previous = token;
                    }
                    lexxBuilder = null;
                    break;
                default:
                    if (lexxBuilder == null) {
                        lexxBuilder = new StringBuilder();
                        list.add(new Token(lexxBuilder));
                    }
                    lexxBuilder.append(ch);
            }
        }
        final Token[] tokens = list.toArray(new Token[0]);

        int years = 0;
        int months = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        int milliseconds = 0;

        // years
        if (Token.containsTokenWithValue(tokens, "y")) {
            years = (int) (durationMillis / 31557600000L);
            durationMillis = durationMillis - (years * 31557600000L);
        }
        // months
        if (Token.containsTokenWithValue(tokens, "M")) {
            months = (int) (durationMillis / 2629800000L);
            durationMillis = durationMillis - (months * 2629800000L);
        }
        // days
        if (Token.containsTokenWithValue(tokens, "d")) {
            days = (int) (durationMillis / 86400000);
            durationMillis = durationMillis - (days * 86400000L);
        }
        // hours
        if (Token.containsTokenWithValue(tokens, "H")) {
            hours = (int) (durationMillis / 3600000);
            durationMillis = durationMillis - (hours * 3600000L);
        }
        // minutes
        if (Token.containsTokenWithValue(tokens, "m")) {
            minutes = (int) (durationMillis / 60000);
            durationMillis = durationMillis - (minutes * 60000L);
        }
        // seconds
        if (Token.containsTokenWithValue(tokens, "s")) {
            seconds = (int) (durationMillis / 1000);
            durationMillis = durationMillis - (seconds * 1000L);
        }
        // milliseconds
        if (Token.containsTokenWithValue(tokens, "S")) milliseconds = (int) durationMillis;

        final StringBuilder builder = new StringBuilder();
        boolean lastOutputSeconds = false;
        for (final Token token : tokens) {
            final String value = token.value.toString();
            final int count = token.count;
            switch (value) {
                case "y":
                    builder.append(padWithZeros ? padWithZeros(Integer.toString(years), count) : Integer.toString(years));
                    lastOutputSeconds = false;
                    break;
                case "M":
                    builder.append(padWithZeros ? padWithZeros(Integer.toString(months), count) : Integer.toString(months));
                    lastOutputSeconds = false;
                    break;
                case "d":
                    builder.append(padWithZeros ? padWithZeros(Integer.toString(days), count) : Integer.toString(days));
                    lastOutputSeconds = false;
                    break;
                case "H":
                    builder.append(padWithZeros ? padWithZeros(Integer.toString(hours), count) : Integer.toString(hours));
                    lastOutputSeconds = false;
                    break;
                case "m":
                    builder.append(padWithZeros ? padWithZeros(Integer.toString(minutes), count) : Integer.toString(minutes));
                    lastOutputSeconds = false;
                    break;
                case "s":
                    builder.append(padWithZeros ? padWithZeros(Integer.toString(seconds), count) : Integer.toString(seconds));
                    lastOutputSeconds = true;
                    break;
                case "S":
                    final String millisecondsString = padWithZeros ? padWithZeros(Integer.toString(milliseconds), count) : Integer.toString(milliseconds);
                    if (!lastOutputSeconds) {
                        builder.append(millisecondsString);
                        break;
                    }
                    milliseconds += 1000;
                    builder.append(millisecondsString.substring(1));
                    lastOutputSeconds = false;
                    break;
                default:
                    builder.append(value);
                    lastOutputSeconds = false;
                    break;
            }
        }

        return builder.toString();
    }

    /**
     * Constructs a new {@link DurationFormatUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private DurationFormatUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Element that is parsed from the format pattern
     */
    private static class Token {
        @NotNull private final StringBuilder value;
        private int count = 1;

        /**
         * Wraps a token around a value. A value would be something like a 'Y'
         *
         * @param   value   to wrap
         */
        private Token(@NotNull StringBuilder value) {
            this.value = value;
        }

        /**
         * Helper method to determine if a set of tokens contain a value
         *
         * @param   tokens  set to look in
         * @param   value   to look for
         *
         * @return          boolean <code>true</code> if contained
         */
        @Contract(pure = true)
        static boolean containsTokenWithValue(@NotNull Token[] tokens, @NotNull String value) {
            for (final Token token : tokens) if (token.value.toString().equals(value)) return true;
            return false;
        }
    }
}
