package xyz.srnyx.annoyingapi.utility;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility class for adding and getting data from item stacks
 */
@SuppressWarnings("deprecation")
public class ItemDataUtility {
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
        this.item = item;
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
        // 1.13+ (persistent data container, custom item tag container, or lore)
        if (ReflectionUtility.namespacedKeyConstructor != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;

            // 1.14+ (persistent data container)
            if (ReflectionUtility.getPdcMethod != null && ReflectionUtility.pdcGetMethod != null && ReflectionUtility.pdtStringField != null) try {
                return (String) ReflectionUtility.pdcGetMethod.invoke(ReflectionUtility.getPdcMethod.invoke(meta), ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, key), ReflectionUtility.pdtStringField);
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }

            // 1.13.2 (custom item tag container)
            if (ReflectionUtility.getCtcMethod != null && ReflectionUtility.ctcGetCustomTagMethod != null) try {
                return (String) ReflectionUtility.ctcGetCustomTagMethod.invoke(ReflectionUtility.getCtcMethod.invoke(meta), ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, key), ReflectionUtility.ittStringClass);
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }

            // 1.13-1.13.1 (lore)
            if (!meta.hasLore()) return null;
            final List<String> lore = meta.getLore();
            for (int i = lore.size() - 1; i >= 0; i--) { // Reverse search because it's more likely to be at the end
                final String line = lore.get(i);
                if (line.startsWith(key + ": ")) return line.substring(key.length() + 2);
            }
            return null;
        }

        // 1.12- (enchantment with ID)
        final int hashCode = key.hashCode();
        for (final Enchantment enchantment : item.getEnchantments().keySet()) if (enchantment.getId() == hashCode) return enchantment.getName();
        return null;
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
    public ItemDataUtility set(@NotNull String key, @NotNull String value) {
        // 1.13+ (persistent data container, custom item tag container, or lore)
        if (ReflectionUtility.namespacedKeyConstructor != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (ReflectionUtility.getPdcMethod != null && ReflectionUtility.pdcSetMethod != null) try {
                ReflectionUtility.pdcSetMethod.invoke(ReflectionUtility.getPdcMethod.invoke(meta), ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, key), ReflectionUtility.pdtStringField, value);
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (ReflectionUtility.getCtcMethod != null && ReflectionUtility.ctcSetCustomTagMethod != null) try {
                ReflectionUtility.ctcSetCustomTagMethod.invoke(ReflectionUtility.getCtcMethod.invoke(meta), ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, key), ReflectionUtility.ittStringClass, value);
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
                return this;
            }

            // 1.13-1.13.1 (lore)
            if (get(key) != null) remove(key); // Check if the key already exists
            final List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(key + ": " + value);
            meta.setLore(lore);
            item.setItemMeta(meta);
            return this;
        }

        // 1.12- (enchantment with ID)
        if (get(key) != null) remove(key); // Check if the key already exists
        item.addUnsafeEnchantment(createEnchantment(key, value), 1);
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
        // 1.13+ (persistent data container, custom item tag container, or lore)
        if (ReflectionUtility.namespacedKeyConstructor != null) {
            final ItemMeta meta = item.getItemMeta();
            if (meta == null) return this;

            // 1.14+ (persistent data container)
            if (ReflectionUtility.getPdcMethod != null && ReflectionUtility.pdcRemoveMethod != null) try {
                ReflectionUtility.pdcRemoveMethod.invoke(ReflectionUtility.getPdcMethod.invoke(meta), ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, key));
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
                return this;
            }

            // 1.13.2 (custom item tag container)
            if (ReflectionUtility.getCtcMethod != null && ReflectionUtility.ctcRemoveCustomTagMethod != null) try {
                ReflectionUtility.ctcRemoveCustomTagMethod.invoke(ReflectionUtility.getCtcMethod.invoke(meta), ReflectionUtility.namespacedKeyConstructor.newInstance(plugin, key));
                item.setItemMeta(meta);
                return this;
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
                return this;
            }

            // 1.13-1.13.1 (lore)
            if (!meta.hasLore()) return this;
            final List<String> lore = meta.getLore();
            for (int i = lore.size() - 1; i >= 0; i--) if (lore.get(i).startsWith(key + ": ")) { // Reverse search because it's more likely to be at the end
                lore.remove(i);
                meta.setLore(lore);
                break;
            }
            item.setItemMeta(meta);
            return this;
        }

        // 1.12- (enchantment with ID)
        final int hashCode = key.hashCode();
        for (final Enchantment enchantment : item.getEnchantments().keySet()) if (enchantment.getId() == hashCode) {
            item.removeEnchantment(enchantment);
            break;
        }
        return this;
    }

    /**
     * Create an enchantment with the given name
     *
     * @param   key     the key of the enchantment (will be hashed using {@link String#hashCode()})
     * @param   value   the name of the enchantment
     *
     * @return          the newly created {@link Enchantment}
     */
    @NotNull @Contract("_, _ -> new")
    private Enchantment createEnchantment(@NotNull String key, @NotNull String value) {
        return new Enchantment(key.hashCode()) {
            @Override @NotNull
            public String getName() {
                return value;
            }
            @Override
            public int getMaxLevel() {
                return 1;
            }
            @Override
            public int getStartLevel() {
                return 1;
            }
            @Override @NotNull
            public EnchantmentTarget getItemTarget() {
                return EnchantmentTarget.ALL;
            }
            @Override
            public boolean isTreasure() {
                return false;
            }
            @Override
            public boolean isCursed() {
                return false;
            }
            @Override
            public boolean conflictsWith(@NotNull Enchantment enchantment) {
                return false;
            }
            @Override
            public boolean canEnchantItem(@NotNull ItemStack itemStack) {
                return true;
            }
        };
    }
}
