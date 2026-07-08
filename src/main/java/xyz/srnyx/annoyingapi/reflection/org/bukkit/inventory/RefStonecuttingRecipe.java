package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.newNamespacedKey;


/**
 * 1.14+ org.bukkit.inventory.StonecuttingRecipe
 */
public class RefStonecuttingRecipe {
    /**
     * 1.14+ org.bukkit.inventory.StonecuttingRecipe
     */
    @Nullable public static final Class<?> STONECUTTING_RECIPE_CLASS = ReflectionUtility.getClass(1, 14, 0, RefStonecuttingRecipe.class);

    /**
     * 1.14+ org.bukkit.inventory.StonecuttingRecipe(org.bukkit.NamespacedKey, org.bukkit.inventory.ItemStack, org.bukkit.Material)
     */
    @Nullable public static final java.lang.reflect.Constructor<?> STONECUTTING_RECIPE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 14, 0, STONECUTTING_RECIPE_CLASS, NAMESPACED_KEY_CLASS, org.bukkit.inventory.ItemStack.class, org.bukkit.Material.class);

    @Nullable
    public static Recipe newStonecuttingRecipe(@Nullable Object namespacedKey, @NotNull ItemStack result, @NotNull Material material) {
        if (STONECUTTING_RECIPE_CONSTRUCTOR == null || namespacedKey == null) return null;

        try {
            return (Recipe) STONECUTTING_RECIPE_CONSTRUCTOR.newInstance(namespacedKey, result, material);
        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Recipe newStonecuttingRecipe(@NotNull Plugin plugin, @NotNull String key, @NotNull ItemStack result, @NotNull Material material) {
        return newStonecuttingRecipe(newNamespacedKey(plugin, key), result, material);
    }

    private RefStonecuttingRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
