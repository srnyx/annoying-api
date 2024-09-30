package xyz.srnyx.annoyingapi.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Registrable;

import xyz.srnyx.javautilities.parents.Stringable;


/**
 * A more advanced version of {@link PlayerMoveEvent}
 */
public class AdvancedPlayerMoveEvent extends PlayerMoveEvent {
    /**
     * The {@link HandlerList} for this event.
     */
    @NotNull private static final HandlerList HANDLERS = new HandlerList();

    /**
     * The {@link MovementType type of movement} that the player has done
     */
    @Nullable private MovementType movementType;

    /**
     * Constructs a new {@link AdvancedPlayerMoveEvent}
     *
     * @param   player  {@link #getPlayer()}
     * @param   from    {@link #getFrom()}
     * @param   to      {@link #getTo()}
     */
    public AdvancedPlayerMoveEvent(@NotNull Player player, @NotNull Location from, @Nullable Location to) {
        super(player, from, to);
    }

    /**
     * Constructs a new {@link AdvancedPlayerMoveEvent} from a {@link PlayerMoveEvent}
     *
     * @param   event   the {@link PlayerMoveEvent} to construct from
     */
    public AdvancedPlayerMoveEvent(@NotNull PlayerMoveEvent event) {
        this(event.getPlayer(), event.getFrom(), event.getTo());
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

    /**
     * Returns the {@link MovementType type of movement} that the player has done
     *
     * @return  {@link #movementType}
     */
    @NotNull
    public MovementType getMovementType() {
        if (movementType != null) return movementType;
        final Location from = getFrom();
        final Location to = getTo();
        if (to == null) return MovementType.TRANSLATION;
        if (from.getYaw() == to.getYaw() && from.getPitch() == to.getPitch()) {
            movementType = MovementType.TRANSLATION;
        } else if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            movementType = MovementType.ROTATION;
        } else {
            movementType = MovementType.BOTH;
        }
        return movementType;
    }

    /**
     * The type of movement that the player has done
     */
    public enum MovementType {
        /**
         * The player has changed their position (X, Y, or Z)
         */
        TRANSLATION,
        /**
         * The player has changed their rotation (yaw or pitch)
         */
        ROTATION,
        /**
         * The player has changed both their position (X, Y, or Z) and their rotation (yaw or pitch)
         */
        BOTH
    }

    /**
     * Handles {@link AdvancedPlayerMoveEvent}
     */
    @Registrable.Ignore
    public static class Handler extends CustomEventHandler {
        /**
         * Constructs a new handler for {@link AdvancedPlayerMoveEvent}
         *
         * @param   plugin  the plugin
         */
        public Handler(@NotNull AnnoyingPlugin plugin) {
            super(plugin);
        }

        /**
         * Holds information for player movement events
         *
         * @param   event   the event
         *
         * @see             AdvancedPlayerMoveEvent
         */
        @EventHandler
        public void onPlayerMove(@NotNull PlayerMoveEvent event) {
            final AdvancedPlayerMoveEvent newEvent = new AdvancedPlayerMoveEvent(event);
            Bukkit.getPluginManager().callEvent(newEvent);
            event.setCancelled(newEvent.isCancelled());
            event.setFrom(newEvent.getFrom());
            final Location to = newEvent.getTo();
            if (to != null) event.setTo(to);
        }
    }
}
