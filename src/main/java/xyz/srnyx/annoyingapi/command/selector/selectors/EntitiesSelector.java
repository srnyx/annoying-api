package xyz.srnyx.annoyingapi.command.selector.selectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.Selector;

import java.util.List;


/**
 * Selector that selects all entities in the sender's world, or the first world if the sender is not a player
 */
public class EntitiesSelector implements Selector<Entity> {
    @Override @NotNull
    public Class<Entity> getType() {
        return Entity.class;
    }

    @Override @NotNull
    public List<Entity> expand(@NotNull AnnoyingSender sender) {
        return sender.getPlayerOptional()
                .map(Entity::getWorld)
                .orElseGet(() -> Bukkit.getWorlds().get(0))
                .getEntities();
    }

    /**
     * Constructor for EntitiesSelector
     */
    public EntitiesSelector() {
        // Only exists to give the constructor a Javadoc
    }
}
