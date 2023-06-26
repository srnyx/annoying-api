package xyz.srnyx.annoyingapi.utility;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Utility methods for relationships between {@link ConfigurationSection ConfigurationSections} and {@link Map Maps}
 */
public class ConfigurationUtility {
    /**
     * Converts a {@link Map} to a {@link ConfigurationSection}
     *
     * @param   map the map to convert
     *
     * @return      the converted configuration section
     */
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

    /**
     * Converts a {@link List} of {@link Map Maps} to a {@link List} of {@link ConfigurationSection ConfigurationSections}
     *
     * @param   maps    the maps to convert
     *
     * @return          the converted configuration sections
     */
    @NotNull
    public static List<ConfigurationSection> toConfigurationList(@NotNull List<Map<?, ?>> maps) {
        final List<ConfigurationSection> configurations = new ArrayList<>();
        maps.forEach(map -> configurations.add(toConfiguration(map)));
        return configurations;
    }

    /**
     * Converts a {@link ConfigurationSection} to a {@link Map}
     *
     * @param   section the section to convert
     *
     * @return          the converted map
     */
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

    /**
     * Converts a {@link List} of {@link ConfigurationSection ConfigurationSections} to a {@link List} of {@link Map Maps}
     *
     * @param   sections    the sections to convert
     *
     * @return              the converted maps
     */
    @NotNull
    public static List<Map<String, Object>> toMapList(@NotNull List<ConfigurationSection> sections) {
        final List<Map<String, Object>> maps = new ArrayList<>();
        sections.forEach(section -> maps.add(toMap(section)));
        return maps;
    }

    /**
     * Constructs a new {@link ConfigurationUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private ConfigurationUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
