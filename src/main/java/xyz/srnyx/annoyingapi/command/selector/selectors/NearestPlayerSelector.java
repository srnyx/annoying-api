package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.*;


/**
 * Selector that selects the nearest player to the command sender
 */
public class NearestPlayerSelector extends Selector<Player> {
    private static final Set<Class<? extends CommandSender>> ALLOWED_SENDERS = new HashSet<>(Arrays.asList(Entity.class, BlockCommandSender.class));

    @Override @NotNull
    public Class<Player> getType() {
        return Player.class;
    }

    @Override @NotNull
    public Set<Class<? extends CommandSender>> getAllowedSenders() {
        return ALLOWED_SENDERS;
    }

    @Override @Nullable
    public List<Player> expandImplementation(@NotNull AnnoyingSender sender) {
        // Get location of sender
        final Location location = sender.getLocationOfSender();
        if (location == null) return null;

        // Get nearest player, ignoring self if applicable
        Player nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        final Player player = sender.getPlayerOrNull();
        for (final Player other : location.getWorld().getPlayers()) {
            if (other.equals(player)) continue; // Skip self
            final double distanceSquared = other.getLocation().distanceSquared(location);
            if (distanceSquared < nearestDistanceSquared) {
                nearest = other;
                nearestDistanceSquared = distanceSquared;
            }
        }
        return nearest == null ? Collections.emptyList() : Collections.singletonList(nearest);
    }

    /**
     * Constructor for NearestPlayerSelector
     */
    public NearestPlayerSelector() {
        // Only exists to give the constructor a Javadoc
    }
}
