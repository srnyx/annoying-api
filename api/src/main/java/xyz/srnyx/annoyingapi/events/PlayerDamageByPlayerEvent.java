package xyz.srnyx.annoyingapi.events;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public class PlayerDamageByPlayerEvent extends EntityDamageByEntityEvent {
    @NotNull private static final HandlerList HANDLERS = new HandlerList();

    public PlayerDamageByPlayerEvent(@NotNull Player damager, @NotNull Player damagee, @NotNull DamageCause cause, double damage) {
        super(damager, damagee, cause, damage);
    }

    @NotNull @Contract(pure = true)
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override @NotNull
    public Player getDamager() {
        return (Player) super.getDamager();
    }

    @Override @NotNull
    public Player getEntity() {
        return (Player) super.getEntity();
    }

    @Override @NotNull
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }

    @NotNull
    public Player getDamagee() {
        return getEntity();
    }
}
