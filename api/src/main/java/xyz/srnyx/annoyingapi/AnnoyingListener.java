package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;


/**
 * A listener that can be registered to the Bukkit event system
 */
public interface AnnoyingListener extends Listener {
    /**
     * The {@link AnnoyingPlugin} that this listener belongs to
     *
     * @return  the plugin instance
     */
    @NotNull
    AnnoyingPlugin getPlugin();

    /**
     * Returns whether the listener is registered to the {@link #getPlugin()}
     *
     * @return  whether the listener is registered
     */
    default boolean isRegistered() {
        return getPlugin().registeredListeners.contains(this);
    }

    /**
     * Toggles the registration of the listener to the {@link #getPlugin()}
     *
     * @param   registered  whether the listener should be registered or unregistered
     */
    default void setRegistered(boolean registered) {
        if (registered) {
            register();
            return;
        }
        unregister();
    }

    /**
     * Registers the listener to the {@link #getPlugin()}
     */
    default void register() {
        if (isRegistered()) return;
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        getPlugin().registeredListeners.add(this);
    }

    /**
     * Unregisters the listener from the {@link #getPlugin()}
     */
    default void unregister() {
        if (!isRegistered()) return;
        HandlerList.unregisterAll(this);
        getPlugin().registeredListeners.remove(this);
    }
}
