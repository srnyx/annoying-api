package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;


/**
 * 1.14+ org.bukkit.inventory.StonecuttingRecipe
 */
public class StonecuttingRecipe {
    /**
     * 1.14+ org.bukkit.inventory.StonecuttingRecipe
     */
    @Nullable public static final Class<?> STONECUTTING_RECIPE_CLASS = ReflectionUtility.getClass(1, 14, 0, StonecuttingRecipe.class);

    /**
     * 1.14+ org.bukkit.inventory.StonecuttingRecipe(org.bukkit.NamespacedKey, org.bukkit.inventory.ItemStack, org.bukkit.Material)
     */
    @Nullable public static final java.lang.reflect.Constructor<?> STONECUTTING_RECIPE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 14, 0, STONECUTTING_RECIPE_CLASS, NAMESPACED_KEY_CLASS, org.bukkit.inventory.ItemStack.class, org.bukkit.Material.class);

    private StonecuttingRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
