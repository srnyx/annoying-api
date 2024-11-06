package xyz.srnyx.annoyingapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a value that may be null
 * <br>This is needed so that the plugin can know whether a value was cached or not for data (because null could be a valid value)
 */
public class Value {
    /**
     * The value
     */
    @Nullable public final String value;

    /**
     * Constructs a new value with the given value
     *
     * @param   value   the value
     */
    public Value(@Nullable String value) {
        this.value = value;
    }

    /**
     * Constructs a new value with a null value
     */
    public Value() {
        this.value = null;
    }

    @Override @NotNull
    public String toString() {
        return value == null ? "null" : value;
    }
}
