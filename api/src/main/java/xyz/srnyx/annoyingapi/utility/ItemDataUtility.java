package xyz.srnyx.annoyingapi.utility;

import de.tr7zw.changeme.nbtapi.NBTItem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.parents.Annoyable;

import java.util.logging.Level;

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
public class ItemDataUtility implements Annoyable {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * The {@link ItemStack} to manage data for
     */
    @NotNull public final ItemStack item;

    /**
     * Construct a new {@link ItemDataUtility} for the given item stack and key
     *
     * @param   plugin  {@link #plugin}
     * @param   item    {@link #item}
     */
    public ItemDataUtility(@NotNull AnnoyingPlugin plugin, @NotNull ItemStack item) {
        this.plugin = plugin;
        this.item = new ItemStack(item);
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    /**
     * Get the data value for the given item stack and key
     *
     * @param   key the key to get the data value for
     *
     * @return      the data value, or null if not found
     */
    @Nullable
    public String get(@NotNull String key) {
        // 1.13.2+ (persistent data container, custom item tag container, or lore)
        if (NAMESPACED_KEY_CONSTRUCTOR != null) {
            final ItemMeta meta = item.getItemMeta();
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
        return new NBTItem(item).getString(key);
    }

    /**
     * Set the data value for the given item stack and key. If the key already exists, it will be overwritten
     *
     * @param   key     the key to set the data value for
     * @param   value   the data value to set
     *
     * @return          this {@link ItemDataUtility} instance
     */
    @NotNull
    public ItemDataUtility set(@NotNull String key, @Nullable Object value) {
        if (value == null) return remove(key); // Remove the value if it's null
        final String string = value.toString();

        // 1.13.2+ (persistent data container or custom item tag container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_SET_METHOD != null) try {
                PERSISTENT_DATA_CONTAINER_SET_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), PERSISTENT_DATA_TYPE_STRING, string);
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("set");
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD != null && CUSTOM_ITEM_TAG_CONTAINER_SET_CUSTOM_TAG_METHOD != null) try {
                CUSTOM_ITEM_TAG_CONTAINER_SET_CUSTOM_TAG_METHOD.invoke(ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key), ITEM_TAG_TYPE_STRING, string);
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("set");
                e.printStackTrace();
                return this;
            }
        }

        // 1.13.1- (NBT API)
        new NBTItem(item, true).setString(key, string);
        return this;
    }

    /**
     * Remove the data value with the given key from the item stack
     *
     * @param   key the key to remove the data value for
     *
     * @return      this {@link ItemDataUtility} instance
     */
    @NotNull @SuppressWarnings("UnusedReturnValue")
    public ItemDataUtility remove(@NotNull String key) {
        // 1.13.2+ (persistent data container or custom item tag container)
        if (NAMESPACED_KEY_CONSTRUCTOR != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD != null && PERSISTENT_DATA_CONTAINER_REMOVE_METHOD != null) try {
                PERSISTENT_DATA_CONTAINER_REMOVE_METHOD.invoke(PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key));
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("remove");
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD != null && CUSTOM_ITEM_TAG_CONTAINER_REMOVE_CUSTOM_TAG_METHOD != null) try {
                CUSTOM_ITEM_TAG_CONTAINER_REMOVE_CUSTOM_TAG_METHOD.invoke(ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD.invoke(meta), NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, key));
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("remove");
                e.printStackTrace();
                return this;
            }
        }

        // 1.13.1- (NBT API)
        new NBTItem(item, true).removeKey(key);
        return this;
    }

    /**
     * Send an error message to the console
     *
     * @param   action  the action that failed
     */
    private void sendError(@NotNull String action) {
        AnnoyingPlugin.log(Level.WARNING, "&cFailed to " + action + " item data for &4" + item.getType() + "&c!");
    }
}
