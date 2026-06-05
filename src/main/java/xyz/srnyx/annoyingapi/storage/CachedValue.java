package xyz.srnyx.annoyingapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a value that may be null
 * <br>This is needed so that the plugin can know whether a value was cached or not for data (because null could be a valid value)
 *
 * @param value The value
 */
public record CachedValue(@Nullable String value) {
    /**
     * Constructs a new value with a null value
     */
    public CachedValue() {
        this(null);
    }

    @Override @NotNull
    public String toString() {
        return value == null ? "null" : value;
    }
}
