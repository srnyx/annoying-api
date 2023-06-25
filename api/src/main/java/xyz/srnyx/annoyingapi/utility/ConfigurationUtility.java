package xyz.srnyx.annoyingapi.utility;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConfigurationUtility {
    @NotNull
    public static ConfigurationSection toConfiguration(@NotNull Map<?, ?> map) {
        final MemoryConfiguration configuration = new MemoryConfiguration();
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                configuration.set(key.toString(), toConfiguration((Map<?, ?>) value));
                return;
            }
            configuration.set(key.toString(), value);
        });
        return configuration;
    }

    @NotNull
    public static List<ConfigurationSection> toConfigurationList(@NotNull List<Map<?, ?>> maps) {
        final List<ConfigurationSection> configurations = new ArrayList<>();
        maps.forEach(map -> configurations.add(toConfiguration(map)));
        return configurations;
    }

    @NotNull
    public static Map<String, Object> toMap(@NotNull ConfigurationSection section) {
        final Map<String, Object> map = new HashMap<>();
        section.getKeys(false).forEach(key -> {
            final Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                map.put(key, toMap((ConfigurationSection) value));
                return;
            }
            map.put(key, value);
        });
        return map;
    }

    @NotNull
    public static List<Map<String, Object>> toMapList(@NotNull List<ConfigurationSection> sections) {
        final List<Map<String, Object>> maps = new ArrayList<>();
        sections.forEach(section -> maps.add(toMap(section)));
        return maps;
    }
}
