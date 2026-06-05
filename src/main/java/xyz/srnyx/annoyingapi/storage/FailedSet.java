package xyz.srnyx.annoyingapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a failed set operation
 *
 * @param table     The table that the set operation failed on
 * @param target    The target that the set operation failed for
 * @param column    The column that the set operation failed on
 * @param value     The value that the set operation failed with
 * @param exception The exception that occurred while attempting to set the value
 */
public record FailedSet(@NotNull String table, @NotNull String target, @NotNull String column, @Nullable String value, @Nullable Throwable exception) {
    /**
     * Constructs a new failed set with the given table, target, column, and value
     *
     * @param table     {@link #table}
     * @param target    {@link #target}
     * @param column    {@link #column}
     * @param value     {@link #value}
     */
    public FailedSet(@NotNull String table, @NotNull String target, @NotNull String column, @Nullable String value) {
        this(table, target, column, value, null);
    }
}
