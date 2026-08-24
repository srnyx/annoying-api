package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class A0001_Rename_kebab_case_to_snake_case extends NamedMigration {
    public A0001_Rename_kebab_case_to_snake_case() {
        super("renames kebab-case keys to snake_case", (config, view) -> {
            final KeysToMoveReturn keysToMoveReturn = getKeysToMove(view.getInternalState(), "");
            for (final String key : keysToMoveReturn.keys()) move(key, key.replace("-", "_")).migrate(config, view);
            return keysToMoveReturn.listMapsUpdated() || !keysToMoveReturn.keys().isEmpty();
        });
    }

    @NotNull
    private static KeysToMoveReturn getKeysToMove(@NotNull Map<?, ?> document, @NotNull String prefix) {
        final List<String> keys = new ArrayList<>();
        boolean listMapsUpdated = false;
        for (final Map.Entry<?, ?> entry : document.entrySet()) {
            final String key = entry.getKey().toString();

            // Add key if it needs to be moved
            if (key.contains("-")) keys.add(prefix + key);

            final Object value = entry.getValue();
            if (value instanceof Map map) {
                // Add children keys (if value is map)
                final String newPrefix = prefix + key.replace("-", "_") + ".";
                final KeysToMoveReturn childKeysToMoveReturn = getKeysToMove(map, newPrefix);
                listMapsUpdated = listMapsUpdated || childKeysToMoveReturn.listMapsUpdated();
                keys.addAll(childKeysToMoveReturn.keys());
            } else if (value instanceof List list) {
                // Migrate children keys (if value is list of maps)
                listMapsUpdated = migrateListMaps(list) || listMapsUpdated;
            }
        }
        return new KeysToMoveReturn(listMapsUpdated, keys);
    }

    private static boolean migrateListMaps(@NotNull List list) {
        if (list.isEmpty()) return false;
        boolean updated = false;
        for (final Object object : list) if (object instanceof Map map) updated = migrateMap(map) || updated;
        return updated;
    }

    private static boolean migrateMap(@NotNull Map map) {
        boolean updated = false;
        for (final Map.Entry<Object, Object> childEntry : new HashMap<Object, Object>(map).entrySet()) {
            final String childKey = childEntry.getKey().toString();
            final Object childValue = childEntry.getValue();

            // Migrate child if it's another Map or List of maps
            if (childValue instanceof Map childMap) {
                updated = migrateMap(childMap) || updated;
            } else if (childValue instanceof List childList) {
                updated = migrateListMaps(childList) || updated;
            }

            // Migrate root
            if (!childKey.contains("-")) continue;
            updated = true;
            map.put(childKey.replace("-", "_"), map.remove(childKey));
        }
        return updated;
    }

    private record KeysToMoveReturn(boolean listMapsUpdated, @NotNull List<String> keys) {}
}
