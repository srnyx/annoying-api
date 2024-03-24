package xyz.srnyx.annoyingapi.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
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
     * Construct a new {@link EntityData} for the given entity
     *
     * @param   plugin  {@link #plugin}
     * @param   entity  {@link #target}
     */
    public EntityData(@NotNull AnnoyingPlugin plugin, @NotNull Entity entity) {
        super(plugin, TABLE_NAME, entity.getUniqueId().toString());
    }

    /**
     * Convert all data from the old data storage system (PDC/file) to the new one (SQL)
     * <br>This <b>does not</b> run automatically, you must call this method manually (for example, on {@link org.bukkit.event.player.PlayerJoinEvent PlayerJoinEvent})!
     * <br>For 1.14+ (PDC), the entity will receive the {@code api_converted} key which indicates that the data has been converted, this will avoid duplicate conversion checks
     * <br>All old data (PDC/file) will be removed after conversion (to avoid duplicate/overwriting data)
     *
     * @param   keys    the names of the keys to convert
     *
     * @return          {@code true} if the conversion was successful, {@code false} otherwise
     */
    public boolean convertOldData(@NotNull Collection<String> keys) {
        if (keys.isEmpty()) return true;

        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_GET_METHOD != null && PERSISTENT_DATA_CONTAINER_SET_METHOD != null && PERSISTENT_DATA_CONTAINER_REMOVE_METHOD != null && PERSISTENT_DATA_TYPE_STRING != null && PERSISTENT_DATA_TYPE_BYTE != null) {
            try {
                final Object persistentDataContainer = PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(target);
                final Object convertedKey = NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, "api_converted");
                // Check if already converted
                if (PERSISTENT_DATA_CONTAINER_GET_METHOD.invoke(persistentDataContainer, convertedKey, PERSISTENT_DATA_TYPE_BYTE) != null) return true;
                // Convert
                boolean success = true;
                for (final String key : keys) {
                    final Object namespacedKey = NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key);
                    final String value = (String) PERSISTENT_DATA_CONTAINER_GET_METHOD.invoke(persistentDataContainer, namespacedKey, PERSISTENT_DATA_TYPE_STRING);
                    if (value == null) continue;
                    if (!set(key, value)) {
                        success = false;
                        continue;
                    }
                    PERSISTENT_DATA_CONTAINER_REMOVE_METHOD.invoke(persistentDataContainer, namespacedKey);
                }
                // Set converted key
                if (success) PERSISTENT_DATA_CONTAINER_SET_METHOD.invoke(persistentDataContainer, convertedKey, PERSISTENT_DATA_TYPE_BYTE, (byte) 1);
                return true;
            } catch (final ReflectiveOperationException e) {
                sendError("convert", e);
                return false;
            }
        }

        // 1.13.2- (file)
        final AnnoyingData file = new AnnoyingData(plugin, plugin.options.dataOptions.entities.path + "/" + target + ".yml", plugin.options.dataOptions.entities.fileOptions);
        final ConfigurationSection section = file.getConfigurationSection(plugin.options.dataOptions.entities.section);
        if (section == null) return true;
        for (final String key : keys) {
            final String value = section.getString(key);
            if (value != null && set(key, value)) section.set(key, null);
        }
        if (section.getKeys(false).isEmpty()) file.set(plugin.options.dataOptions.entities.section, null);
        file.save();
        return true;
    }

    /**
     * Calls {@link #convertOldData(Collection)} with the given keys
     *
     * @param   keys    the names of the keys to convert
     *
     * @return          {@code true} if the conversion was successful, {@code false} otherwise
     *
     * @see             #convertOldData(Collection)
     */
    public boolean convertOldData(@NotNull String... keys) {
        return convertOldData(Arrays.asList(keys));
    }
}
