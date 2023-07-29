package xyz.srnyx.annoyingapi.utility;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingData;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.util.UUID;
import java.util.logging.Level;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataContainer.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataContainer.PERSISTENT_DATA_CONTAINER_REMOVE_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataHolder.PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataType.PERSISTENT_DATA_TYPE_STRING;


/**
 * Utility class for adding and getting data from entities
 */
public class EntityDataUtility implements Annoyable {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * The {@link Entity} to manage data for
     */
    @NotNull private final Entity entity;
    /**
     * 1.13.2- The {@link AnnoyingData} file that contains the data for the entity
     */
    @Nullable private AnnoyingData file;
    /**
     * 1.13.2- The {@link ConfigurationSection} in the {@link #file} that contains the data for the entity
     */
    @Nullable private ConfigurationSection section;

    /**
     * Construct a new {@link EntityDataUtility} for the given entity
     *
     * @param   plugin  {@link #plugin}
     * @param   entity  {@link #entity}
     */
    public EntityDataUtility(@NotNull AnnoyingPlugin plugin, @NotNull Entity entity) {
        this.plugin = plugin;
        this.entity = entity;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Get the data value for the given key
     *
     * @param   key the key to get the data value for
     *
     * @return      the data value, or null if not found
     */
    @Nullable
    public String get(@NotNull String key) {
        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_GET_METHOD != null && PERSISTENT_DATA_TYPE_STRING != null) try {
            return (String) PERSISTENT_DATA_CONTAINER_GET_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(entity), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), PERSISTENT_DATA_TYPE_STRING);
        } catch (final ReflectiveOperationException e) {
            sendError("get");
            e.printStackTrace();
            return null;
        }

        // 1.13.2- (file)
        return getSection().getString(key);
    }

    /**
     * Set the data value for the given key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set
     *
     * @return          this {@link EntityDataUtility} instance
     */
    @NotNull
    public EntityDataUtility set(@NotNull String key, @Nullable Object value) {
        if (value == null) return remove(key);
        final String string = value.toString();

        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_SET_METHOD != null) try {
            PERSISTENT_DATA_CONTAINER_SET_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(entity), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), PERSISTENT_DATA_TYPE_STRING, string);
            return this;
        } catch (final ReflectiveOperationException e) {
            sendError("set");
            e.printStackTrace();
            return this;
        }

        // 1.13.2- (file)
        getSection().set(key, string);
        getFile().save();
        return this;
    }

    /**
     * Remove the data value with the given key
     *
     * @param   key the key to remove the data value for
     *
     * @return      this {@link EntityDataUtility} instance
     */
    @NotNull
    public EntityDataUtility remove(@NotNull String key) {
        // 1.14+ (persistent data container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null && PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_REMOVE_METHOD != null) try {
            PERSISTENT_DATA_CONTAINER_REMOVE_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(entity), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key));
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
        final UUID uuid = entity.getUniqueId();

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

    /**
     * Send an error message to the console
     *
     * @param   action  the action that failed
     */
    private void sendError(@NotNull String action) {
        AnnoyingPlugin.log(Level.WARNING, "&cFailed to " + action + " entity data for &4" + entity.getName() + "&c!");
    }
}
