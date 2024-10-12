package xyz.srnyx.annoyingapi.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingData;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataContainer.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataHolder.PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataType.PERSISTENT_DATA_TYPE_BYTE;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataType.PERSISTENT_DATA_TYPE_STRING;


/**
 * Utility class for adding and getting data from entities
 */
public class EntityData extends StringData {
    /**
     * The name of the table in the database to store entity data
     */
    @NotNull public static final String TABLE_NAME = "entities";

    /**
     * The entity to manage data for (only used for {@link #convertOldData(boolean, Collection)})
     */
    @NotNull private final Entity entity;

    /**
     * Construct a new {@link EntityData} for the given entity
     *
     * @param   plugin  {@link #plugin}
     * @param   entity  {@link #entity} and {@link #target} (uses entity UUID)
     */
    public EntityData(@NotNull AnnoyingPlugin plugin, @NotNull Entity entity) {
        super(plugin, TABLE_NAME, entity.getUniqueId().toString());
        this.entity = entity;
    }

    /**
     * Convert all data from the old data storage system (PDC/file) to the new one (SQL)
     * <br>This <b>does not</b> run automatically, you must call this method manually (for example, on {@link org.bukkit.event.player.PlayerJoinEvent PlayerJoinEvent})!
     * <br>For 1.14+ (PDC), the entity will receive the {@code api_converted} key which indicates that the data has been converted, this will avoid duplicate conversion checks
     * <br>All old data (PDC/file) will be removed after conversion (to avoid duplicate/overwriting data)
     *
     * @param   onlyTryOnce 1.14+ only | {@code true} to only try once to convert the data, even if it fails (so if run again, nothing will happen). If {@code false}, the conversion failed previously, and this is run again, it will try to convert again. If the data is successfully converted, this option doesn't matter
     * @param   keys        only applicable for 1.14-1.16, otherwise it will convert all keys (no matter what is provided)
     *
     * @return              a map of keys that failed to convert (key, value) or {@code null} if an error occurred (only returns {@code null} if 1.14+ fails)
     */
    @Nullable @SuppressWarnings("deprecation")
    public Map<String, String> convertOldData(boolean onlyTryOnce, @Nullable Collection<String> keys) {
        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_GET_METHOD != null && PERSISTENT_DATA_CONTAINER_SET_METHOD != null && PERSISTENT_DATA_TYPE_STRING != null && PERSISTENT_DATA_TYPE_BYTE != null) {
            final Object persistentDataContainer;
            final Object convertedKey;
            try {
                // Get PDC
                persistentDataContainer = PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(entity);
                // Check if already converted
                convertedKey = NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, "annoyingapi_converted");
                if (PERSISTENT_DATA_CONTAINER_GET_METHOD.invoke(persistentDataContainer, convertedKey, PERSISTENT_DATA_TYPE_BYTE) != null) return Collections.emptyMap();
                // Set converted key if onlyTryOnce is true
                if (onlyTryOnce) PERSISTENT_DATA_CONTAINER_SET_METHOD.invoke(persistentDataContainer, convertedKey, PERSISTENT_DATA_TYPE_BYTE, (byte) 1);
            } catch (final ReflectiveOperationException e) {
                sendError("convert", e);
                return null;
            }

            // Get keys
            final Map<String, Object> namespacedKeys = new HashMap<>();
            if (PERSISTENT_DATA_CONTAINER_GET_KEYS_METHOD != null && NAMESPACED_KEY_GET_NAMESPACE_METHOD != null && NAMESPACED_KEY_GET_KEY_METHOD != null) {
                // 1.16.1+ (getKeys)
                final String pluginName = plugin.getName().toLowerCase();
                try {
                    for (final Object namespacedKey : (Set<?>) PERSISTENT_DATA_CONTAINER_GET_KEYS_METHOD.invoke(persistentDataContainer)) {
                        final String namespace = (String) NAMESPACED_KEY_GET_NAMESPACE_METHOD.invoke(namespacedKey);
                        if (!pluginName.equals(namespace.toLowerCase())) continue;
                        final String key = (String) NAMESPACED_KEY_GET_KEY_METHOD.invoke(namespacedKey);
                        namespacedKeys.put(key, namespacedKey);
                    }
                } catch (final ReflectiveOperationException e) {
                    sendError("convert", e);
                    return null;
                }
            } else {
                // 1.14-1.16 (provided keys)
                if (keys == null || keys.isEmpty()) return Collections.emptyMap();
                try {
                     for (final String key : keys) {
                         final Object namespacedKey = NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key);
                         namespacedKeys.put(key, namespacedKey);
                     }
                } catch (final ReflectiveOperationException e) {
                    sendError("convert", e);
                    return null;
                }
            }

            try {
                // Convert
                final Map<String, String> failed = new HashMap<>();
                for (final Map.Entry<String, Object> entry : namespacedKeys.entrySet()) {
                    final String key = entry.getKey();
                    final Object namespacedKey = entry.getValue();
                    final String value;
                    try {
                        value = (String) PERSISTENT_DATA_CONTAINER_GET_METHOD.invoke(persistentDataContainer, namespacedKey, PERSISTENT_DATA_TYPE_STRING);
                    } catch (final InvocationTargetException e) {
                        failed.put(key, null);
                        continue;
                    }
                    if (value != null && !set(key, value)) failed.put(key, value);
                }
                // Set converted key (set earlier if onlyTryOnce is true)
                if (!onlyTryOnce && failed.isEmpty()) PERSISTENT_DATA_CONTAINER_SET_METHOD.invoke(persistentDataContainer, convertedKey, PERSISTENT_DATA_TYPE_BYTE, (byte) 1);
                // Return failures
                return failed;
            } catch (final ReflectiveOperationException e) {
                sendError("convert", e);
                return null;
            }
        }

        // 1.13.2- (file)
        final AnnoyingData file = new AnnoyingData(plugin, plugin.options.dataOptions.entities.path + "/" + target + ".yml", plugin.options.dataOptions.entities.fileOptions);
        final ConfigurationSection section = file.getConfigurationSection(plugin.options.dataOptions.entities.section);
        if (section == null || section.getBoolean("annoyingapi.converted")) return Collections.emptyMap();
        // Convert
        final Map<String, String> failed = new HashMap<>();
        for (final String key : section.getKeys(false)) {
            final String value = section.getString(key);
            if (value != null && !set(key, value)) failed.put(key, value);
        }
        // Set converted key
        if (failed.isEmpty() || onlyTryOnce) {
            section.set("annoyingapi.converted", true);
            file.save();
        }
        // Return failures
        return failed;
    }

    /**
     * Calls {@link #convertOldData(boolean, Collection)} with the given keys
     *
     * @param   onlyTryOnce 1.14+ only | {@code true} to only try once to convert the data, even if it fails (so if run again, nothing will happen). If {@code false}, the conversion failed previously, and this is run again, it will try to convert again. If the data is successfully converted, this option doesn't matter
     * @param   keys        only applicable for 1.14-1.16, otherwise it will convert all keys (no matter what is provided)
     *
     * @return              a map of keys that failed to convert (key, value) or {@code null} if an error occurred (only returns {@code null} if 1.14+ fails)
     *
     * @see                 #convertOldData(boolean, Collection)
     */
    @Nullable
    public Map<String, String> convertOldData(boolean onlyTryOnce, @NotNull String... keys) {
        return convertOldData(onlyTryOnce, Arrays.asList(keys));
    }

    /**
     * Calls {@link #convertOldData(boolean, Collection)} with {@code onlyTryOnce} set to {@code false} and the given keys
     *
     * @param   keys    only applicable for 1.14-1.16, otherwise it will convert all keys (no matter what is provided)
     *
     * @return          a map of keys that failed to convert (key, value) or {@code null} if an error occurred (only returns {@code null} if 1.14+ fails)
     *
     * @see             #convertOldData(boolean, Collection)
     */
    @Nullable
    public Map<String, String> convertOldData(@NotNull Collection<String> keys) {
        return convertOldData(false, keys);
    }

    /**
     * Calls {@link #convertOldData(boolean, Collection)} with {@code onlyTryOnce} set to {@code false} and the given keys
     *
     * @param   keys    only applicable for 1.14-1.16, otherwise it will convert all keys (no matter what is provided)
     *
     * @return          a map of keys that failed to convert (key, value) or {@code null} if an error occurred (only returns {@code null} if 1.14+ fails)
     *
     * @see             #convertOldData(boolean, Collection)
     */
    @Nullable
    public Map<String, String> convertOldData(@NotNull String... keys) {
        return convertOldData(false, keys);
    }

    /**
     * Calls {@link #convertOldData(boolean, Collection)} with {@code onlyTryOnce} set to {@code true} and {@code keys} set to {@code null}
     *
     * @param   onlyTryOnce 1.14+ only | {@code true} to only try once to convert the data, even if it fails (so if run again, nothing will happen). If {@code false}, the conversion failed previously, and this is run again, it will try to convert again. If the data is successfully converted, this option doesn't matter
     *
     * @return              a map of keys that failed to convert (key, value) or {@code null} if an error occurred (only returns {@code null} if 1.14+ fails)
     *
     * @see                 #convertOldData(boolean, Collection)
     */
    @Nullable
    public Map<String, String> convertOldData(boolean onlyTryOnce) {
        return convertOldData(onlyTryOnce, (Collection<String>) null);
    }

    /**
     * Calls {@link #convertOldData(boolean, Collection)} with {@code onlyTryOnce} set to {@code false} and {@code keys} set to {@code null}
     *
     * @return              a map of keys that failed to convert (key, value) or {@code null} if an error occurred (only returns {@code null} if 1.14+ fails)
     *
     * @see                 #convertOldData(boolean, Collection)
     */
    @Nullable
    public Map<String, String> convertOldData() {
        return convertOldData(false, (Collection<String>) null);
    }
}
