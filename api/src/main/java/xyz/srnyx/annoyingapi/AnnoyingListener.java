package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import xyz.srnyx.annoyingapi.parents.Registrable;


/**
 * A listener that can be registered to the Bukkit event system
 */
public abstract class AnnoyingListener extends Registrable implements Listener {
    /**
     * Registers the listener to the {@link #getAnnoyingPlugin()}
     */
    @Override
    public void register() {
        if (isRegistered()) return;
        Bukkit.getPluginManager().registerEvents(this, getAnnoyingPlugin());
        super.register();
    }

    /**
     * Unregisters the listener from the {@link #getAnnoyingPlugin()}
     */
    @Override
    public void unregister() {
        if (!isRegistered()) return;
        HandlerList.unregisterAll(this);
        super.unregister();
    }
}
