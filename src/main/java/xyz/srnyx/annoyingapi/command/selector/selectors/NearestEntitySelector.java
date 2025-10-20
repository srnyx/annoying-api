package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.Collections;
import java.util.List;


/**
 * Selector that selects the nearest entity to the command sender
 */
public class NearestEntitySelector implements Selector<Entity> {
    @Override @NotNull
    public Class<Entity> getType() {
        return Entity.class;
    }

    @Override @Nullable
    public List<Entity> expand(@NotNull AnnoyingSender sender) {
        final Player player = sender.getPlayerOrNull();
        if (player == null) return null;
        final Location playerLocation = player.getLocation();

        // Get nearest entity
        Entity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (final Entity entity : player.getWorld().getEntities()) {
            if (entity.equals(player)) continue; // Skip self
            final double distanceSquared = entity.getLocation().distanceSquared(playerLocation);
            if (distanceSquared < nearestDistanceSquared) {
                nearest = entity;
                nearestDistanceSquared = distanceSquared;
            }
        }
        return nearest == null ? Collections.emptyList() : Collections.singletonList(nearest);
    }

    /**
     * Constructor for NearestEntitySelector
     */
    public NearestEntitySelector() {
        // Only exists to give the constructor a Javadoc
    }
}
