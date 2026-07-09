package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;


public class AnnoyingConfig extends OkaeriConfig {
    @NotNull private static final String PATH_SEPARATOR = ".";

    public <T> T getAs(@NotNull String key, @NotNull Class<T> type) {
        // Root
        if (!key.contains(PATH_SEPARATOR)) return this.get(key, type);

        // Deep traversal
        Object current = this;
        final String[] parts = key.split(Pattern.quote(PATH_SEPARATOR));
        for (int i = 0; i < parts.length; i++) {
            current = ((OkaeriConfig) current).get(parts[i]);
            if (current == null) return null;

            if (i == parts.length - 1) return type.cast(current);

            if (!(current instanceof OkaeriConfig)) return null;
        }
        return null;
    }
}
