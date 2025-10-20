package xyz.srnyx.annoyingapi.command.selector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingSender;
import xyz.srnyx.annoyingapi.command.selector.selectors.*;

import xyz.srnyx.javautilities.MapGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Interface for target selectors for commands
 *
 * @param   <T> the type of object the selector expands to
 */
public interface Selector<T> {
    /**
     * Gets the type of object the selector expands to
     *
     * @return  the type of object the selector expands to
     */
    @NotNull
    Class<T> getType();

    /**
     * Expands the selector to a list of objects
     *
     * @param   sender  the {@link AnnoyingSender} who executed the command
     *
     * @return          the list of objects the selector expands to, or null if none
     */
    @Nullable
    List<T> expand(@NotNull AnnoyingSender sender);

    /**
     * Map of all registered selectors with their keys
     */
    @NotNull Map<String, Selector<?>> SELECTORS = MapGenerator.LINKED_HASH_MAP.mapOf(
            "@all_players", new AllPlayersSelector(),
            "@online", new OnlineSelector(),
            "@offline", new OfflineSelector(),
            "@self", new SelfSelector(),
            "@nearest_player", new NearestPlayerSelector(),
            "@random_player", new RandomPlayerSelector(),
            "@entities", new EntitiesSelector(),
            "@nearest_entity", new NearestEntitySelector(),
            "@random_entity", new RandomEntitySelector());

    /**
     * Gets a list of all registered selector keys
     *
     * @return  a list of all registered selector keys
     */
    @NotNull
    static List<String> getKeys() {
        return new ArrayList<>(SELECTORS.keySet());
    }

    /**
     * Gets a list of all registered selector keys for a specific type
     *
     * @param   type    the type to filter by
     *
     * @return          a list of all registered selector keys for the specified type
     */
    @NotNull
    static List<String> getKeys(@NotNull Class<?> type) {
        final List<String> keys = new ArrayList<>();
        for (final Map.Entry<String, Selector<?>> entry : SELECTORS.entrySet()) if (type.isAssignableFrom(entry.getValue().getType())) keys.add(entry.getKey());
        return keys;
    }

    /**
     * Adds all registered selector keys to the beginning of a collection
     *
     * @param   collection  the collection to add to
     *
     * @return              the collection with all registered selector keys added
     */
    @NotNull
    static Collection<String> addKeys(@NotNull Collection<String> collection) {
        final List<String> result = getKeys();
        result.addAll(collection);
        return result;
    }

    /**
     * Adds all registered selector keys for a specific type to the beginning of a collection
     *
     * @param   collection  the collection to add to
     * @param   type        the type to filter by
     *
     * @return              the collection with all registered selector keys for the specified type added
     */
    @NotNull
    static List<String> addKeys(@NotNull Collection<String> collection, @NotNull Class<?> type) {
        final List<String> result = getKeys(type);
        result.addAll(collection);
        return result;
    }
}
