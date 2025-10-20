package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import xyz.srnyx.javautilities.MiscUtility;

import java.util.Collections;
import java.util.List;


/**
 * Selector that selects a random entity in the sender's world, or the first world if the sender is not a player
 */
public class RandomEntitySelector implements Selector<Entity> {
    @Override @NotNull
    public Class<Entity> getType() {
        return Entity.class;
    }

    @Override @NotNull
    public List<Entity> expand(@NotNull AnnoyingSender sender) {
        final List<Entity> entities = sender.getPlayerOptional()
                .map(Entity::getWorld)
                .orElseGet(() -> Bukkit.getWorlds().get(0))
                .getEntities();
        final int size = entities.size();
        return size == 0 ? Collections.emptyList() : Collections.singletonList(entities.get(MiscUtility.RANDOM.nextInt(size)));
    }

    /**
     * Constructor for RandomEntitySelector
     */
    public RandomEntitySelector() {
        // Only exists to give the constructor a Javadoc
    }
}
