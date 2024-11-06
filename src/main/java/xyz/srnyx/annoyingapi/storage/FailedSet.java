package xyz.srnyx.annoyingapi.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a failed set operation
 */
public class FailedSet {
    /**
     * The table that the set operation failed on
     */
    @NotNull public final String table;
    /**
     * The target that the set operation failed for
     */
    @NotNull public final String target;
    /**
     * The column that the set operation failed on
     */
    @NotNull public final String column;
    /**
     * The value that the set operation failed with
     */
    @Nullable public final String value;
    /**
     * The exception that occurred while attempting to set the value
     */
    @Nullable public final Throwable exception;

    /**
     * Constructs a new failed set with the given table, target, column, and value
     *
     * @param   table   {@link #table}
     * @param   target  {@link #target}
     * @param   column  {@link #column}
     * @param   value   {@link #value}
     */
    public FailedSet(@NotNull String table, @NotNull String target, @NotNull String column, @Nullable String value) {
        this.table = table;
        this.target = target;
        this.column = column;
        this.value = value;
        this.exception = null;
    }

    /**
     * Constructs a new failed set with the given table, target, column, value, and exception
     *
     * @param   table       {@link #table}
     * @param   target      {@link #target}
     * @param   column      {@link #column}
     * @param   value       {@link #value}
     * @param   exception   {@link #exception}
     */
    public FailedSet(@NotNull String table, @NotNull String target, @NotNull String column, @Nullable String value, @NotNull Throwable exception) {
        this.table = table;
        this.target = target;
        this.column = column;
        this.value = value;
        this.exception = exception;
    }
}
