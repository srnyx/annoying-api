package xyz.srnyx.annoyingapi.command.selector;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.command.selector.selectors.*;

import java.util.LinkedHashMap;
import java.util.Map;


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
}
