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


public interface Selector<T> {
    @NotNull
    Class<T> getType();

    @Nullable
    List<T> expand(@NotNull AnnoyingSender sender);

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

    @NotNull
    static List<String> getKeys() {
        return new ArrayList<>(SELECTORS.keySet());
    }

    @NotNull
    static List<String> getKeys(@NotNull Class<?> type) {
        final List<String> keys = new ArrayList<>();
        for (final Map.Entry<String, Selector<?>> entry : SELECTORS.entrySet()) if (type.isAssignableFrom(entry.getValue().getType())) keys.add(entry.getKey());
        return keys;
    }

    @NotNull
    static Collection<String> addKeys(@NotNull Collection<String> collection) {
        final List<String> result = getKeys();
        result.addAll(collection);
        return result;
    }

    @NotNull
    static List<String> addKeys(@NotNull Collection<String> collection, @NotNull Class<?> type) {
        final List<String> result = getKeys(type);
        result.addAll(collection);
        return result;
    }
}
