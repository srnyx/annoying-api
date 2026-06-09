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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Selector that selects the nearest entity to the command sender
 */
public class NearestEntitySelector extends Selector<Entity> {
    private static final Set<Class<? extends CommandSender>> ALLOWED_SENDERS = new HashSet<>(Arrays.asList(Entity.class, BlockCommandSender.class));

    @Override @NotNull
    public Class<Entity> getType() {
        return Entity.class;
    }

    @Override @NotNull
    public Set<Class<? extends CommandSender>> getAllowedSenders() {
        return ALLOWED_SENDERS;
    }

    @Override @Nullable
    public List<Entity> expandImplementation(@NotNull AnnoyingSender sender) {
        // Get location of sender
        final Location location = sender.getLocationOfSender();
        if (location == null) return null;

        // Get nearest entity, ignoring self if applicable
        Entity nearest = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        final Player player = sender.getPlayerOrNull();
        for (final Entity entity : location.getWorld().getEntities()) {
            if (entity.equals(player)) continue; // Skip self
            final double distanceSquared = entity.getLocation().distanceSquared(location);
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
