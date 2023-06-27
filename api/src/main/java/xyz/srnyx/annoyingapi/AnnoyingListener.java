package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import xyz.srnyx.annoyingapi.parents.Annoyable;


/**
 * A listener that can be registered to the Bukkit event system
 */
public interface AnnoyingListener extends Listener, Annoyable {
    /**
     * Returns whether the listener is registered to the {@link #getAnnoyingPlugin()}
     *
     * @return  whether the listener is registered
     */
    default boolean isRegistered() {
        return getAnnoyingPlugin().registeredListeners.contains(this);
    }

    /**
     * Toggles the registration of the listener to the {@link #getAnnoyingPlugin()}
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
     * Registers the listener to the {@link #getAnnoyingPlugin()}
     */
    default void register() {
        if (isRegistered()) return;
        Bukkit.getPluginManager().registerEvents(this, getAnnoyingPlugin());
        getAnnoyingPlugin().registeredListeners.add(this);
    }

    /**
     * Unregisters the listener from the {@link #getAnnoyingPlugin()}
     */
    default void unregister() {
        if (!isRegistered()) return;
        HandlerList.unregisterAll(this);
        getAnnoyingPlugin().registeredListeners.remove(this);
    }
}
