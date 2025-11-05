package xyz.srnyx.annoyingapi.command.selector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;

import java.util.List;


/**
 * Interface for target selectors for commands
 *
 * @param   <T> the type of object the selector expands to
 */
public interface Selector<T> {
    /**
     * Gets the type of object the selector expands to
     *
     * @return  the type of object the selector expands to
     */
    @NotNull
    Class<T> getType();

    /**
     * Expands the selector to a list of objects
     *
     * @param   sender  the {@link AnnoyingSender} who executed the command
     *
     * @return          the list of objects the selector expands to, or null if none
     */
    @Nullable
    List<T> expand(@NotNull AnnoyingSender sender);
}
