package xyz.srnyx.annoyingapi.command.selector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;

import java.util.*;
import java.util.function.Function;


/**
 * A class representing an optional selector.
 *
 * @param   <T> the type of the selector
 */
public class SelectorOptional<T> {
    /**
     * The sender who executed the command
     */
    @NotNull private final AnnoyingSender sender;
    /**
     * The raw input string before being parsed as a selector, if present
     */
    @Nullable private final String raw;
    /**
     * The selector, if present
     */
    @Nullable private final Selector<T> selector;

    /**
     * Constructor for SelectorOptional
     *
     * @param   sender   the sender who executed the command
     * @param   raw      the raw input string before being parsed as a selector, if present
     * @param   selector the selector, if present
     */
    public SelectorOptional(@NotNull AnnoyingSender sender, @Nullable String raw, @Nullable Selector<T> selector) {
        this.sender = sender;
        this.raw = raw;
        this.selector = selector;
    }

    /**
     * Gets the sender who executed the command
     *
     * @return  the sender
     */
    @NotNull
    public AnnoyingSender getSender() {
        return sender;
    }

    /**
     * Gets the raw input string before being parsed as a selector
     *
     * @return  the raw input string
     *
     * @throws  IllegalStateException   if called before checking {@link #isEmpty()}
     */
    @NotNull
    public String getRaw() {
        if (raw == null) throw new IllegalStateException("getRaw() called before checking isEmpty()");
        return raw;
    }

    /**
     * Gets the selector
     *
     * @return  the selector
     *
     * @throws  IllegalStateException   if called before checking {@link #isEmpty()}
     */
    @NotNull
    public Selector<T> getSelector() {
        if (selector == null) throw new IllegalStateException("getSelector() called before checking isEmpty()");
        return selector;
    }

    /**
     * Checks if the selector is present
     *
     * @return  true if the selector is present, false otherwise
     */
    public boolean isPresent() {
        return selector != null;
    }

    /**
     * Checks if the selector is absent
     *
     * @return  true if the selector is absent, false otherwise
     */
    public boolean isEmpty() {
        return selector == null;
    }

    /**
     * Applies a function to the {@link #raw} input string if present (after selector parsing)
     *
     * @param   mapper  the function to map the raw input string
     *
     * @return          a new SelectorOptional with the mapped raw input string
     */
    @NotNull
    public SelectorOptional<T> mapRaw(@NotNull Function<String, String> mapper) {
        return raw == null ? this : new SelectorOptional<>(sender, mapper.apply(raw), selector);
    }

    /**
     * Expands the selector if present, otherwise uses the provided function to get a default value
     *
     * @param   other   the function to get a default value if the selector is absent. The raw input string (after mappings) is provided as an argument.
     *
     * @return          the expanded selector or the default value
     */
    @Nullable
    public List<T> orElse(@NotNull Function<String, List<T>> other) {
        if (selector == null) return other.apply(raw);
        final List<T> expanded = selector.expand(sender);
        if (expanded == null) sender.invalidArgument(raw);
        return expanded;
    }

    /**
     * Expands the selector if present, otherwise uses the provided function to get a single default value
     *
     * @param   other   the function to get a single default value if the selector is absent. The raw input string (after mappings) is provided as an argument.
     *
     * @return          the expanded selector or a singleton list containing the default value
     */
    @Nullable
    public List<T> orElseSingle(@NotNull Function<String, T> other) {
        if (selector == null) return Collections.singletonList(other.apply(raw));
        final List<T> expanded = selector.expand(sender);
        if (expanded == null) sender.invalidArgument(raw);
        return expanded;
    }

    /**
     * Expands the selector if present, otherwise uses the provided function to get an optional default value
     *
     * @param   other   the function to get an optional default value if the selector is absent. The raw input string (after mappings) is provided as an argument.
     *
     * @return          the expanded selector or the default value
     */
    @Nullable
    public List<T> orElseFlat(@NotNull Function<String, Optional<List<T>>> other) {
        if (selector == null) {
            final Optional<List<T>> result = other.apply(raw);
            if (!result.isPresent()) sender.invalidArgument(raw);
            return result.orElse(null);
        }
        final List<T> expanded = selector.expand(sender);
        if (expanded == null) sender.invalidArgument(raw);
        return expanded;
    }

    /**
     * Expands the selector if present, otherwise uses the provided function to get an optional single default value
     *
     * @param   other   the function to get an optional single default value if the selector is absent. The raw input string (after mappings) is provided as an argument.
     *
     * @return          the expanded selector or a singleton list containing the default value
     */
    @Nullable
    public List<T> orElseFlatSingle(@NotNull Function<String, Optional<T>> other) {
        if (selector == null) {
            final Optional<T> result = other.apply(raw);
            if (!result.isPresent()) {
                sender.invalidArgument(raw);
                return null;
            }
            return Collections.singletonList(result.get());
        }
        final List<T> expanded = selector.expand(sender);
        if (expanded == null) sender.invalidArgument(raw);
        return expanded;
    }

    /**
     * Creates a SelectorOptional with no argument
     *
     * @param   sender  the sender who executed the command
     *
     * @return          a SelectorOptional with no argument
     *
     * @param   <T>     the type of the selector
     */
    @NotNull
    public static <T> SelectorOptional<T> noArgument(@NotNull AnnoyingSender sender) {
        return new SelectorOptional<>(sender, null, null);
    }

    /**
     * Creates a SelectorOptional with no selector
     *
     * @param   sender  the sender who executed the command
     * @param   raw     the raw input string that was attempted to be parsed as a selector
     *
     * @return          a SelectorOptional with no selector
     *
     * @param   <T>     the type of the selector
     */
    @NotNull
    public static <T> SelectorOptional<T> noSelector(@NotNull AnnoyingSender sender, @Nullable String raw) {
        return new SelectorOptional<>(sender, raw, null);
    }

    /**
     * Creates a SelectorOptional with no type filtering from the given input string
     *
     * @param   sender  the sender who executed the command
     * @param   input   the input string to parse as a selector
     *
     * @return          a SelectorOptional with the parsed selector, or no selector if none matched
     *
     * @param   <T>     the type of the selector
     */
    @NotNull
    public static <T> SelectorOptional<T> of(@NotNull AnnoyingSender sender, @NotNull String input) {
        final String inputLower = input.toLowerCase();
        for (final Map.Entry<String, Selector<?>> entry : sender.plugin.selectorManager.selectors.entrySet()) {
            if (entry.getKey().equals(inputLower)) {
                //noinspection unchecked
                return new SelectorOptional<>(sender, input, (Selector<T>) entry.getValue());
            }
        }
        return SelectorOptional.noSelector(sender, input);
    }

    /**
     * Creates a SelectorOptional with type filtering from the given input string
     *
     * @param   sender  the sender who executed the command
     * @param   input   the input string to parse as a selector
     * @param   type    the type to filter selectors by
     *
     * @return          a SelectorOptional with the parsed selector, or no selector if none matched
     *
     * @param   <T>     the type of the selector
     */
    @NotNull
    public static <T> SelectorOptional<T> of(@NotNull AnnoyingSender sender, @NotNull String input, @NotNull Class<T> type) {
        final String inputLower = input.toLowerCase();
        for (final Map.Entry<String, Selector<?>> entry : sender.plugin.selectorManager.selectors.entrySet()) {
            if (entry.getKey().equals(inputLower) && type.isAssignableFrom(entry.getValue().getType())) {
                //noinspection unchecked
                return new SelectorOptional<>(sender, input, (Selector<T>) entry.getValue());
            }
        }
        return SelectorOptional.noSelector(sender, input);
    }
}
