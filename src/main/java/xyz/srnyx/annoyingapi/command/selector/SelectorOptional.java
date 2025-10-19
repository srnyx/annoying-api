package xyz.srnyx.annoyingapi.command.selector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;

import java.util.*;
import java.util.function.Function;


public class SelectorOptional<T> {
    @NotNull private final AnnoyingSender sender;
    @Nullable private final String raw;
    @Nullable private final Selector<T> selector;

    public SelectorOptional(@NotNull AnnoyingSender sender, @Nullable String raw, @Nullable Selector<T> selector) {
        this.sender = sender;
        this.raw = raw;
        this.selector = selector;
    }

    @NotNull
    public AnnoyingSender getSender() {
        return sender;
    }

    @NotNull
    public String getRaw() {
        if (raw == null) throw new IllegalStateException("getRaw() called before checking isEmpty()");
        return raw;
    }

    @NotNull
    public Selector<T> getSelector() {
        if (selector == null) throw new IllegalStateException("getSelector() called before checking isEmpty()");
        return selector;
    }

    public boolean isPresent() {
        return selector != null;
    }

    public boolean isEmpty() {
        return selector == null;
    }

    @NotNull
    public SelectorOptional<T> mapRaw(@NotNull Function<String, String> mapper) {
        return raw == null ? this : new SelectorOptional<>(sender, mapper.apply(raw), selector);
    }

    @Nullable
    public List<T> orElse(@NotNull Function<String, List<T>> other) {
        if (selector == null) return other.apply(raw);
        final List<T> expanded = selector.expand(sender);
        if (expanded == null) sender.invalidArgument(raw);
        return expanded;
    }

    @Nullable
    public List<T> orElseSingle(@NotNull Function<String, T> other) {
        if (selector == null) return Collections.singletonList(other.apply(raw));
        final List<T> expanded = selector.expand(sender);
        if (expanded == null) sender.invalidArgument(raw);
        return expanded;
    }

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

    @NotNull
    public static <T> SelectorOptional<T> noArgument(@NotNull AnnoyingSender sender) {
        return new SelectorOptional<>(sender, null, null);
    }

    @NotNull
    public static <T> SelectorOptional<T> noSelector(@NotNull AnnoyingSender sender, @Nullable String raw) {
        return new SelectorOptional<>(sender, raw, null);
    }

    @NotNull
    public static <T> SelectorOptional<T> of(@NotNull AnnoyingSender sender, @NotNull String input) {
        final String inputLower = input.toLowerCase();
        for (final Map.Entry<String, Selector<?>> entry : Selector.SELECTORS.entrySet()) {
            if (entry.getKey().equals(inputLower)) {
                //noinspection unchecked
                return new SelectorOptional<>(sender, input, (Selector<T>) entry.getValue());
            }
        }
        return SelectorOptional.noSelector(sender, input);
    }

    @NotNull
    public static <T> SelectorOptional<T> of(@NotNull AnnoyingSender sender, @NotNull String input, @NotNull Class<T> type) {
        final String inputLower = input.toLowerCase();
        for (final Map.Entry<String, Selector<?>> entry : Selector.SELECTORS.entrySet()) {
            if (entry.getKey().equals(inputLower) && type.isAssignableFrom(entry.getValue().getType())) {
                //noinspection unchecked
                return new SelectorOptional<>(sender, input, (Selector<T>) entry.getValue());
            }
        }
        return SelectorOptional.noSelector(sender, input);
    }
}
