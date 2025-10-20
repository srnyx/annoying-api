package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.Collections;
import java.util.List;


/**
 * Selector that selects the nearest player to the command sender
 */
public class NearestPlayerSelector implements Selector<Player> {
    @Override @NotNull
    public Class<Player> getType() {
        return Player.class;
    }

    @Override @Nullable
    public List<Player> expand(@NotNull AnnoyingSender sender) {
        final Player player = sender.getPlayerOrNull();
        if (player == null) return null;
        final Location playerLocation = player.getLocation();

        // Get nearest player
        Player nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (final Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue; // Skip self
            final double distanceSquared = other.getLocation().distanceSquared(playerLocation);
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
