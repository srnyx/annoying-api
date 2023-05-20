package xyz.srnyx.annoyingapi.utility;

import de.tr7zw.changeme.nbtapi.NBTItem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.logging.Level;

import static xyz.srnyx.annoyingapi.utility.ReflectionUtility.*;


/**
 * Utility class for adding and getting data from item stacks
 */
public class ItemDataUtility {
    /**
     * The {@link AnnoyingPlugin plugin} instance
     */
    @NotNull private final AnnoyingPlugin plugin;
    /**
     * The {@link ItemStack} to manage data for
     */
    @NotNull public ItemStack item;

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
        if (namespacedKeyConstructor != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;

            // 1.14+ (persistent data container)
            if (getPdcMethod != null && pdcGetMethod != null && pdtStringField != null) try {
                return (String) pdcGetMethod.invoke(getPdcMethod.invoke(meta), namespacedKeyConstructor.newInstance(plugin, key), pdtStringField);
            } catch (final ReflectiveOperationException e) {
                sendError("get");
                e.printStackTrace();
                return null;
            }

            // 1.13.2 (custom item tag container)
            if (getCtcMethod != null && ctcGetCustomTagMethod != null) try {
                return (String) ctcGetCustomTagMethod.invoke(getCtcMethod.invoke(meta), namespacedKeyConstructor.newInstance(plugin, key), ittStringClass);
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
    public ItemDataUtility set(@NotNull String key, @Nullable String value) {
        if (value == null) return remove(key); // Remove the value if it's null

        // 1.13.2+ (persistent data container or custom item tag container)
        if (namespacedKeyConstructor != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (getPdcMethod != null && pdcSetMethod != null) try {
                pdcSetMethod.invoke(getPdcMethod.invoke(meta), namespacedKeyConstructor.newInstance(plugin, key), pdtStringField, value);
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("set");
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (getCtcMethod != null && ctcSetCustomTagMethod != null) try {
                ctcSetCustomTagMethod.invoke(getCtcMethod.invoke(meta), namespacedKeyConstructor.newInstance(plugin, key), ittStringClass, value);
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("set");
                e.printStackTrace();
                return this;
            }
        }

        // 1.13.1- (NBT API)
        final NBTItem nbt = new NBTItem(item);
        nbt.setString(key, value);
        item = nbt.getItem();
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
        if (namespacedKeyConstructor != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (getPdcMethod != null && pdcRemoveMethod != null) try {
                pdcRemoveMethod.invoke(getPdcMethod.invoke(meta), namespacedKeyConstructor.newInstance(plugin, key));
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("remove");
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (getCtcMethod != null && ctcRemoveCustomTagMethod != null) try {
                ctcRemoveCustomTagMethod.invoke(getCtcMethod.invoke(meta), namespacedKeyConstructor.newInstance(plugin, key));
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                sendError("remove");
                e.printStackTrace();
                return this;
            }
        }

        // 1.13.1- (NBT API)
        final NBTItem nbt = new NBTItem(item);
        nbt.removeKey(key);
        item = nbt.getItem();
        return this;
    }

    /**
     * Send an error message to the console
     *
     * @param   action  the action that failed
     */
    private void sendError(@NotNull String action) {
        plugin.log(Level.WARNING, "&cFailed to " + action + " item data for &4" + item.getType() + "&c!");
    }
}
