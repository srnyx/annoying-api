package xyz.srnyx.annoyingapi.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;


public class EventHandlers implements AnnoyingListener {
    @NotNull
    private final AnnoyingPlugin plugin;

    @Contract(pure = true)
    public EventHandlers(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public AnnoyingPlugin getPlugin() {
        return plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity damagee = event.getEntity();
        if (!(damager instanceof Player) || !(damagee instanceof Player)) return;
        final PlayerDamageByPlayerEvent newEvent = new PlayerDamageByPlayerEvent((Player) damager, (Player) damagee, event.getCause(), event.getDamage());
        Bukkit.getPluginManager().callEvent(newEvent);
        event.setCancelled(newEvent.isCancelled());
    }
}
