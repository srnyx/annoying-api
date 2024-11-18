package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Optional;
import java.util.StringJoiner;


/**
 * A utility class for managing arguments
 */
public class Arguments extends Stringable {
    /**
     * The arguments
     */
    @Nullable public final String[] args;

    /**
     * Construct a new {@link Arguments} with the specified arguments
     *
     * @param   args    the arguments
     */
    public Arguments(@Nullable String[] args) {
        this.args = args;
    }

    /**
     * Returns if the specified {@link #args} index is equal to <b>any</b> of the specified strings (case-insensitive)
     *
     * @param   index   the argument index
     * @param   strings the strings to compare to
     *
     * @return          {@code true} if the specified {@link #args} index is equal to <b>any</b> of the specified strings (case-insensitive)
     */
    public boolean argEquals(int index, @Nullable String... strings) {
        if (args == null || args.length <= index) return false;
        final String arg = args[index];
        if (arg == null) return false;
        for (final String string : strings) if (arg.equalsIgnoreCase(string)) return true;
        return false;
    }

    /**
     * Gets the argument at the specified index
     *
     * @param   index   the argument index
     *
     * @return          the argument at the specified index
     */
    @Nullable
    public String getArgument(int index) {
        return args == null || args.length <= index ? null : args[index];
    }

    /**
     * Gets the argument at the specified index as an {@link Optional}
     *
     * @param   index   the argument index
     *
     * @return          the argument at the specified index as an {@link Optional}
     */
    @NotNull
    public Optional<String> getArgumentOptional(int index) {
        return Optional.ofNullable(getArgument(index));
    }

    /**
     * Gets multiple arguments joined together starting from the specified index and ending at the specified index (if too high, it will stop at the last argument)
     * <br>If no arguments are found, it returns an empty string
     *
     * @param   start   the starting index
     * @param   end     the ending index
     *
     * @return          the arguments joined together
     */
    @NotNull
    public String getArgumentsJoined(int start, int end) {
        if (args == null || args.length <= start) return "";
        final StringJoiner joiner = new StringJoiner(" ");
        for (int i = start; i < args.length && i < end; i++) joiner.add(args[i]);
        return joiner.toString();
    }

    /**
     * Gets multiple arguments joined together starting from the specified index
     * <br>If no arguments are found, it returns an empty string
     *
     * @param   start   the starting index
     *
     * @return          the arguments joined together
     */
    @NotNull
    public String getArgumentsJoined(int start) {
        return args == null ? "" : getArgumentsJoined(start, args.length);
    }

    /**
     * Gets all arguments joined together
     * <br>If no arguments are found, it returns an empty string
     *
     * @return  all arguments joined together
     */
    @NotNull
    public String getArgumentsJoined() {
        return args == null ? "" : String.join(" ", args);
    }
}
