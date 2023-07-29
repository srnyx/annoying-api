package xyz.srnyx.annoyingapi.data;

import de.tr7zw.changeme.nbtapi.NBTItem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.RefItemMeta.ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.tags.RefCustomItemTagContainer.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.tags.RefItemTagType.ITEM_TAG_TYPE_STRING;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataContainer.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataHolder.PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence.RefPersistentDataType.PERSISTENT_DATA_TYPE_STRING;


/**
 * Utility class for adding and getting data from item stacks
 */
public class ItemData extends Data<ItemStack> {
    /**
     * Construct a new {@link ItemData} for the given item stack
     *
     * @param   plugin  {@link #plugin}
     * @param   item    {@link #target}
     */
    public ItemData(@NotNull AnnoyingPlugin plugin, @NotNull ItemStack item) {
        super(plugin, new ItemStack(item));
    }

    @Override @NotNull
    public String getTargetName() {
        return target.getType().toString();
    }

    /**
     * Get the data value for the given key
     *
     * @param   key the key to get the data value for
     *
     * @return      the data value, or null if not found
     */
    @Override @Nullable
    public String get(@NotNull String key) {
        // 1.13.2+ (persistent data container or custom item tag container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null) {
            final ItemMeta meta = target.getItemMeta();
            if (meta == null) return null;

            // 1.14+ (persistent data container)
            if (PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_GET_METHOD != null && PERSISTENT_DATA_TYPE_STRING != null) try {
                return (String) PERSISTENT_DATA_CONTAINER_GET_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), PERSISTENT_DATA_TYPE_STRING);
            } catch (final ReflectiveOperationException e) {
                sendError("get");
                e.printStackTrace();
                return null;
            }

            // 1.13.2 (custom item tag container)
            if (ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD != null && CUSTOM_ITEM_TAG_CONTAINER_GET_CUSTOM_TAG_METHOD != null) try {
                return (String) CUSTOM_ITEM_TAG_CONTAINER_GET_CUSTOM_TAG_METHOD.invoke(ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), ITEM_TAG_TYPE_STRING);
            } catch (final ReflectiveOperationException e) {
                sendError("get");
                e.printStackTrace();
                return null;
            }
        }

        // 1.13.1- (NBT API)
        return new NBTItem(target).getString(key);
    }

    /**
     * Set the data value for the given key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set
     *
     * @return          this {@link ItemData} instance
     */
    @Override @NotNull
    protected ItemData set(@NotNull String key, @NotNull String value) {
        // 1.13.2+ (persistent data container or custom item tag container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null) {
            final ItemMeta meta = target.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_SET_METHOD != null) try {
                PERSISTENT_DATA_CONTAINER_SET_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), PERSISTENT_DATA_TYPE_STRING, value);
                target.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("set");
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD != null && CUSTOM_ITEM_TAG_CONTAINER_SET_CUSTOM_TAG_METHOD != null) try {
                CUSTOM_ITEM_TAG_CONTAINER_SET_CUSTOM_TAG_METHOD.invoke(ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), ITEM_TAG_TYPE_STRING, value);
                target.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("set");
                e.printStackTrace();
                return this;
            }
        }

        // 1.13.1- (NBT API)
        new NBTItem(target, true).setString(key, value);
        return this;
    }

    /**
     * Remove the data value with the given key
     *
     * @param   key the key to remove the data value for
     *
     * @return      this {@link ItemData} instance
     */
    @Override @NotNull
    public ItemData remove(@NotNull String key) {
        // 1.13.2+ (persistent data container or custom item tag container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null) {
            final ItemMeta meta = target.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_REMOVE_METHOD != null) try {
                PERSISTENT_DATA_CONTAINER_REMOVE_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key));
                target.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("remove");
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD != null && CUSTOM_ITEM_TAG_CONTAINER_REMOVE_CUSTOM_TAG_METHOD != null) try {
                CUSTOM_ITEM_TAG_CONTAINER_REMOVE_CUSTOM_TAG_METHOD.invoke(ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key));
                target.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("remove");
                e.printStackTrace();
                return this;
            }
        }

        // 1.13.1- (NBT API)
        new NBTItem(target, true).removeKey(key);
        return this;
    }
}
