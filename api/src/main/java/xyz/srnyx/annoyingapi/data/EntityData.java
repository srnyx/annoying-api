package xyz.srnyx.annoyingapi.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingData;

import java.util.UUID;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataContainer.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataContainer.PERSISTENT_DATA_CONTAINER_REMOVE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataHolder.PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataType.PERSISTENT_DATA_TYPE_STRING;


/**
 * Utility class for adding and getting data from entities
 */
public class EntityData extends Data<Entity> {
    /**
     * 1.13.2- The {@link AnnoyingData} file that contains the data for the entity
     */
    @Nullable private AnnoyingData file;
    /**
     * 1.13.2- The {@link ConfigurationSection} in the {@link #file} that contains the data for the entity
     */
    @Nullable private ConfigurationSection section;

    /**
     * Construct a new {@link EntityData} for the given entity
     *
     * @param   plugin  {@link #plugin}
     * @param   entity  {@link #target}
     */
    public EntityData(@NotNull AnnoyingPlugin plugin, @NotNull Entity entity) {
        super(plugin, entity);
    }

    @Override @NotNull
    public String getTargetName() {
        return target.getName();
    }

    @Override @Nullable
    public String get(@NotNull String key) {
        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_GET_METHOD != null && PERSISTENT_DATA_TYPE_STRING != null) try {
            return (String) PERSISTENT_DATA_CONTAINER_GET_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(target), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), PERSISTENT_DATA_TYPE_STRING);
        } catch (final ReflectiveOperationException e) {
            sendError("get");
            e.printStackTrace();
            return null;
        }

        // 1.13.2- (file)
        return getSection().getString(key);
    }

    @Override @NotNull
    protected EntityData set(@NotNull String key, @NotNull String value) {
        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_SET_METHOD != null) try {
            PERSISTENT_DATA_CONTAINER_SET_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(target), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), PERSISTENT_DATA_TYPE_STRING, value);
            return this;
        } catch (final ReflectiveOperationException e) {
            sendError("set");
            e.printStackTrace();
            return this;
        }

        // 1.13.2- (file)
        getSection().set(key, value);
        getFile().save();
        return this;
    }

    @Override @NotNull
    public EntityData remove(@NotNull String key) {
        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_REMOVE_METHOD != null) try {
            PERSISTENT_DATA_CONTAINER_REMOVE_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(target), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key));
            return this;
        } catch (final ReflectiveOperationException e) {
            sendError("remove");
            e.printStackTrace();
            return this;
        }

        // 1.13.2- (file)
        getSection().set(key, null);
        getFile().save();
        return this;
    }

    /**
     * Get {@link #file} or set it if it is null
     *
     * @return  the {@link #file}
     */
    @NotNull
    private AnnoyingData getFile() {
        if (file != null) return file;
        final UUID uuid = target.getUniqueId();

        final AnnoyingData cachedFile = plugin.entityDataFiles.get(uuid);
        if (cachedFile != null) {
            file = cachedFile;
            return file;
        }

        file = new AnnoyingData(plugin, plugin.options.dataOptions.entities.path + "/" + uuid + ".yml", plugin.options.dataOptions.entities.fileOptions);
        plugin.entityDataFiles.put(uuid, file);
        return file;
    }

    /**
     * Get {@link #section} or set it if it is null
     *
     * @return  the {@link #section}
     */
    @NotNull
    private ConfigurationSection getSection() {
        if (section != null) return section;
        section = getFile().getConfigurationSection(plugin.options.dataOptions.entities.section);
        if (section == null) section = getFile().createSection(plugin.options.dataOptions.entities.section);
        return section;
    }
}
