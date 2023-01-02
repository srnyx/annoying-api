package xyz.srnyx.annoyingapi.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingListener;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;


public class PlayerDamageByPlayerEvent extends EntityDamageByEntityEvent {
    @NotNull private static final HandlerList HANDLERS = new HandlerList();

    public PlayerDamageByPlayerEvent(@NotNull Player damager, @NotNull Player damagee, @NotNull DamageCause cause, double damage) {
        super(damager, damagee, cause, damage);
    }

    @Contract(pure = true)
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static class EntityDamageByEntityListener implements AnnoyingListener {
        @NotNull private final AnnoyingPlugin plugin;

        @Contract(pure = true)
        public EntityDamageByEntityListener(@NotNull AnnoyingPlugin plugin) {
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
}
