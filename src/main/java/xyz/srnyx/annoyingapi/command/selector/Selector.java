package xyz.srnyx.annoyingapi.command.selector;

import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;

import java.util.List;
import java.util.Set;


/**
 * Interface for target selectors for commands
 *
 * @param   <T> the type of object the selector expands to
 */
public abstract class Selector<T> {
    /**
     * Gets the type of object the selector expands to
     *
     * @return  the type of object the selector expands to
     */
    @NotNull
    public abstract Class<T> getType();

    /**
     * Gets the allowed senders for the selector
     *
     * @return  the allowed senders for the selector, or null if all senders are allowed
     */
    @Nullable
    public Set<Class<? extends CommandSender>> getAllowedSenders() {
        return null;
    }

    @Nullable
    protected abstract List<T> expandImplementation(@NotNull AnnoyingSender sender);

    /**
     * Expands the selector to a list of objects
     *
     * @param   sender  the {@link AnnoyingSender} who executed the command
     *
     * @return          the list of objects the selector expands to, or null if invalid sender
     */
    @Nullable
    public List<T> expand(@NotNull AnnoyingSender sender) {
        // If no allowed senders, expand immediately
        final Set<Class<? extends CommandSender>> allowedSenders = getAllowedSenders();
        if (allowedSenders == null || allowedSenders.isEmpty()) return expandImplementation(sender);

        // Only expand if sender is allowed
        final Class<? extends CommandSender> senderClass = sender.cmdSender.getClass();
        for (final Class<? extends CommandSender> allowedSender : allowedSenders) {
            if (allowedSender.isAssignableFrom(senderClass)) return expandImplementation(sender);
        }

        // Sender not allowed
        return null;
    }
}
