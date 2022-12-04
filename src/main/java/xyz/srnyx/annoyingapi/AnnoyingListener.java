package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import org.jetbrains.annotations.NotNull;


public interface AnnoyingListener extends Listener {
    /**
     * Registers the listener to the {@link #getPlugin()}
     */
    default void register() {
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    /**
     * Registers a specific {@link Event} from the {@link #getPlugin()}
     * <p><i>The {@link AnnoyingListener} it belongs to <b>must</b> be registered</i>
     *
     * @param   event   the {@link Event} to register
     */
    default void register(@NotNull Event event) {
        HandlerList.getRegisteredListeners(getPlugin()).stream()
                .filter(listener -> listener.getListener() == this)
                .findFirst()
                .ifPresent(listener -> event.getHandlers().register(listener));
    }

    /**
     * Unregisters the listener from the {@link #getPlugin()}
     */
    default void unregister() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Unregisters a specific event from the {@link #getPlugin()}
     *
     * @param   event   the {@link Event} to unregister
     */
    default void unregister(@NotNull Event event) {
        event.getHandlers().unregister(this);
    }

    /**
     * The {@link AnnoyingPlugin} that this listener belongs to
     *
     * @return  the plugin instance
     */
    @NotNull
    AnnoyingPlugin getPlugin();
}
