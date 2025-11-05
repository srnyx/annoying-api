package xyz.srnyx.annoyingapi.command.selector;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.command.selector.selectors.*;

import java.util.*;


public class SelectorManager {
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * Set of registered {@link Selector}s by the plugin
     */
    @NotNull public final Map<String, Selector<?>> selectors = new LinkedHashMap<>();

    public SelectorManager(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerSelectors() {
        // Default selectors
        if (plugin.options.registrationOptions.selectors.enableDefaultSelectors) {
            final char prefix = plugin.options.registrationOptions.selectors.defaultSelectorPrefix;
            selectors.put(prefix + "all_players", new AllPlayersSelector());
            selectors.put(prefix + "online", new OnlineSelector());
            selectors.put(prefix + "offline", new OfflineSelector());
            selectors.put(prefix + "self", new SelfSelector());
            selectors.put(prefix + "nearest_player", new NearestPlayerSelector());
            selectors.put(prefix + "random_player", new RandomPlayerSelector());
            selectors.put(prefix + "entities", new EntitiesSelector());
            selectors.put(prefix + "nearest_entity", new NearestEntitySelector());
            selectors.put(prefix + "random_entity", new RandomEntitySelector());
        }

        // Custom selectors
        selectors.putAll(plugin.options.registrationOptions.selectors.toRegister);
    }

    /**
     * Gets a list of all registered selector keys
     *
     * @return  a list of all registered selector keys
     */
    @NotNull
    public List<String> getKeys() {
        return new ArrayList<>(selectors.keySet());
    }

    /**
     * Gets a list of all registered selector keys for a specific type
     *
     * @param   type    the type to filter by
     *
     * @return          a list of all registered selector keys for the specified type
     */
    @NotNull
    public List<String> getKeys(@NotNull Class<?> type) {
        final List<String> keys = new ArrayList<>();
        for (final Map.Entry<String, Selector<?>> entry : selectors.entrySet()) if (type.isAssignableFrom(entry.getValue().getType())) keys.add(entry.getKey());
        return keys;
    }

    /**
     * Adds all registered selector keys to the beginning of a collection
     * <br><b>This returns a NEW List, it does not modify the input collection!</b>
     *
     * @param   collection  the collection to add to
     *
     * @return              the collection with all registered selector keys added
     *
     * @see                 #addKeysTo(Collection)
     */
    @NotNull
    public Collection<String> withKeys(@NotNull Collection<String> collection) {
        final List<String> result = getKeys();
        result.addAll(collection);
        return result;
    }

    /**
     * Adds all registered selector keys for a specific type to the beginning of a collection
     * <br><b>This returns a NEW List, it does not modify the input collection!</b>
     *
     * @param   collection  the collection to add to
     * @param   type        the type to filter by
     *
     * @return              the collection with all registered selector keys for the specified type added
     *
     * @see                 #addKeysTo(Collection, Class)
     */
    @NotNull
    public List<String> withKeys(@NotNull Collection<String> collection, @NotNull Class<?> type) {
        final List<String> result = getKeys(type);
        result.addAll(collection);
        return result;
    }

    /**
     * Adds all registered selector keys to a collection
     * <br><b>This modifies the input collection!</b>
     *
     * @param   collection  the collection to add to
     *
     * @return              the collection with all registered selector keys added (for convenience)
     *
     * @see                 #withKeys(Collection)
     */
    @NotNull
    public Collection<String> addKeysTo(@NotNull Collection<String> collection) {
        collection.addAll(getKeys());
        return collection;
    }

    /**
     * Adds all registered selector keys for a specific type to a collection
     * <br><b>This modifies the input collection!</b>
     *
     * @param   collection  the collection to add to
     * @param   type        the type to filter by
     *
     * @return              the collection with all registered selector keys added (for convenience)
     *
     * @see                 #withKeys(Collection, Class)
     */
    @NotNull
    public Collection<String> addKeysTo(@NotNull Collection<String> collection, @NotNull Class<?> type) {
        collection.addAll(getKeys(type));
        return collection;
    }
}
