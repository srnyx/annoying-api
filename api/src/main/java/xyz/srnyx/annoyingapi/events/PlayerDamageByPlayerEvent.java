package xyz.srnyx.annoyingapi.events;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.parents.Stringable;
import java.util.EnumMap;


/**
 * This event is called when a {@link Player} damages another {@link Player}
 */
public class PlayerDamageByPlayerEvent extends EntityDamageByEntityEvent {
    /**
     * The {@link HandlerList} for this event.
     */
    @NotNull private static final HandlerList HANDLERS = new HandlerList();
    @SuppressWarnings("Guava")
    @NotNull private static final Function<? super Double, Double> ZERO = o -> -0.0;

    /**
     * Instantiates a new {@link PlayerDamageByPlayerEvent}
     *
     * @param   damager the {@link Player} who damaged the {@code damagee}
     * @param   damagee the {@link Player} who was damaged by the {@code damager}
     * @param   cause   the {@link DamageCause} of the damage
     * @param   damage  the amount of damage dealt
     */
    public PlayerDamageByPlayerEvent(@NotNull Player damager, @NotNull Player damagee, @NotNull DamageCause cause, double damage) {
        super(damager, damagee, cause, new EnumMap<>(ImmutableMap.of(DamageModifier.BASE, damage)), new EnumMap<>(ImmutableMap.of(DamageModifier.BASE, ZERO)));
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

    /**
     * Returns the {@link Player} that damaged the defender
     *
     * @return  {@link Player} that damaged the defender
     */
    @Override @NotNull
    public Player getDamager() {
        return (Player) super.getDamager();
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
}
