package xyz.srnyx.annoyingapi.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import xyz.srnyx.javautilities.parents.Stringable;


/**
 * Class for handling events for the API
 */
public class EventHandlers extends Stringable implements AnnoyingListener {
    /**
     * The plugin instance
     */
    @NotNull
    private final AnnoyingPlugin plugin;

    /**
     * Instantiates a new {@link EventHandlers} for handling API events
     *
     * @param   plugin  the plugin instance
     */
    public EventHandlers(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Holds information for player movement events
     *
     * @param   event   the event
     *
     * @see             AnnoyingPlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        final AnnoyingPlayerMoveEvent newEvent = new AnnoyingPlayerMoveEvent(event);
        newEvent.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(newEvent);
        event.setCancelled(newEvent.isCancelled());
    }

    /**
     * Called when an entity is damaged by an entity
     *
     * @param   event   the event
     *
     * @see             PlayerDamageByPlayerEvent
     */
    @EventHandler
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity damagee = event.getEntity();
        if (!(damager instanceof Player) || !(damagee instanceof Player)) return;
        final PlayerDamageByPlayerEvent newEvent = new PlayerDamageByPlayerEvent((Player) damager, (Player) damagee, event.getCause(), event.getDamage());
        newEvent.setCancelled(event.isCancelled());
        Bukkit.getPluginManager().callEvent(newEvent);
        event.setCancelled(newEvent.isCancelled());
    }
}
