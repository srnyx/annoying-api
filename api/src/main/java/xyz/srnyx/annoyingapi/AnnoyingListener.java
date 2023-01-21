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
     * Registers the listener to the {@link #getPlugin()}
     */
    default void register() {
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        getPlugin().registeredListeners.add(this);
    }

    /**
     * Unregisters the listener from the {@link #getPlugin()}
     */
    default void unregister() {
        HandlerList.unregisterAll(this);
        getPlugin().registeredListeners.remove(this);
    }
}
