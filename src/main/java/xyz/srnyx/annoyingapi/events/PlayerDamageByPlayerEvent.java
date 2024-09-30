package xyz.srnyx.annoyingapi.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Registrable;

import xyz.srnyx.javautilities.parents.Stringable;


/**
 * This event is called when a {@link Player} damages another {@link Player}
 */
public class PlayerDamageByPlayerEvent extends EntityEvent implements Cancellable {
    /**
     * The {@link HandlerList} for this event.
     */
    @NotNull private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Whether this event is cancelled
     */
    private boolean cancelled = false;
    /**
     * The {@link Player} who damaged the defender
     */
    @NotNull private final Player damager;
    /**
     * The {@link EntityDamageEvent.DamageCause} of the damage
     */
    @NotNull private final EntityDamageEvent.DamageCause cause;
    /**
     * The amount of damage dealt
     */
    private double damage;

    /**
     * Instantiates a new {@link PlayerDamageByPlayerEvent}
     *
     * @param   damager {@link #damager}
     * @param   damagee the {@link Player} who was damaged by the {@code damager}
     * @param   cause   {@link #cause}
     * @param   damage  {@link #damage}
     */
    public PlayerDamageByPlayerEvent(@NotNull Player damager, @NotNull Player damagee, @NotNull EntityDamageEvent.DamageCause cause, double damage) {
        super(damagee);
        this.damager = damager;
        this.cause = cause;
        this.damage = damage;
    }

    /**
     * Instantiates a new {@link PlayerDamageByPlayerEvent} from an {@link EntityDamageByEntityEvent}
     *
     * @param   event   the event to instantiate from
     */
    public PlayerDamageByPlayerEvent(@NotNull EntityDamageByEntityEvent event) {
        this((Player) event.getDamager(), (Player) event.getEntity(), event.getCause(), event.getDamage());
        setCancelled(event.isCancelled());
    }

    @Override @NotNull
    public String toString() {
        return Stringable.toString(this);
    }

    /**
     * Returns the {@link HandlerList} for this event
     *
     * @return  {@link #HANDLERS}
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Returns the {@link HandlerList} for this event
     *
     * @return  {@link #HANDLERS}
     */
    @Override @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    /**
     * Returns the {@link Player} that damaged the defender
     *
     * @return  {@link Player} that damaged the defender
     */
    @NotNull
    public Player getDamager() {
        return damager;
    }

    /**
     * Returns the {@link Player} who was damaged
     *
     * @return  {@link Player} who was damaged
     */
    @Override @NotNull
    public Player getEntity() {
        return (Player) super.getEntity();
    }

    /**
     * Same as {@link #getEntity()}, this is just an alias
     *
     * @return  {@link #getEntity()}
     *
     * @see     #getEntity()
     */
    @NotNull
    public Player getDamagee() {
        return getEntity();
    }

    /**
     * Will always return {@link EntityType#PLAYER}
     *
     * @return  {@link EntityType#PLAYER}
     */
    @Override @NotNull
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }

    /**
     * Returns the {@link EntityDamageEvent.DamageCause} of the damage
     *
     * @return  {@link EntityDamageEvent.DamageCause} of the damage
     */
    @NotNull
    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }

    /**
     * Returns the amount of damage dealt
     *
     * @return  the amount of damage dealt
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the amount of damage dealt
     *
     * @param   damage  the amount of damage dealt
     */
    public void setDamage(double damage) {
        this.damage = damage;
    }

    /**
     * Handles {@link PlayerDamageByPlayerEvent}
     */
    @Registrable.Ignore
    public static class Handler extends CustomEventHandler {
        /**
         * Constructs a new handler for {@link PlayerDamageByPlayerEvent}
         *
         * @param   plugin  the plugin
         */
        public Handler(@NotNull AnnoyingPlugin plugin) {
            super(plugin);
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
            if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
            final PlayerDamageByPlayerEvent newEvent = new PlayerDamageByPlayerEvent(event);
            Bukkit.getPluginManager().callEvent(newEvent);
            event.setCancelled(newEvent.isCancelled());
            event.setDamage(newEvent.getDamage());
        }
    }
}
