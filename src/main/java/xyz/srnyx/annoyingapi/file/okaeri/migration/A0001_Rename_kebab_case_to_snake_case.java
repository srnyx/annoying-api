package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;
import eu.okaeri.configs.migrate.view.InternalStateView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class A0001_Rename_kebab_case_to_snake_case extends NamedMigration {
    public A0001_Rename_kebab_case_to_snake_case() {
        super("renames kebab-case keys to snake_case", (config, view) -> {
            if (!(view instanceof InternalStateView internalStateView)) return false;
            final List<String> keys = getKeysToMove(internalStateView.getInternalState(), "");
            for (final String key : keys) move(key, key.replace("-", "_")).migrate(config, view);
            return !keys.isEmpty();
        });
    }

    @NotNull
    private static List<String> getKeysToMove(@NotNull Map<?, ?> document, @NotNull String prefix) {
        final List<String> keys = new ArrayList<>();
        for (final Map.Entry<?, ?> entry : document.entrySet()) {
            final String key = entry.getKey().toString();

            // Add key if it needs to be moved
            if (key.contains("-")) keys.add(prefix + key);

            // Add children keys (if value is map)
            final String newPrefix = prefix + key.replace("-", "_") + ".";
            if (entry.getValue() instanceof Map) keys.addAll(getKeysToMove((Map<?, ?>) entry.getValue(), newPrefix));
        }
        return keys;
    }
}
