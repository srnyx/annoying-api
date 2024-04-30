package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;


/**
 * org.bukkit.inventory.ShapelessRecipe
 */
public class RefShapedRecipe {
    /**
     * 1.12+ org.bukkit.inventory.ShapedRecipe(org.bukkit.NamespacedKey, org.bukkit.inventory.ItemStack)
     */
    @Nullable public static final Constructor<ShapedRecipe> SHAPED_RECIPE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 12, 0, ShapedRecipe.class, RefNamespacedKey.NAMESPACED_KEY_CLASS, ItemStack.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefShapedRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
