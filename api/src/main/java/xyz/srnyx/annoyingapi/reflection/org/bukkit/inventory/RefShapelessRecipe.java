package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;


/**
 * org.bukkit.inventory.ShapelessRecipe
 */
public class RefShapelessRecipe {
    /**
     * 1.12+ org.bukkit.inventory.ShapelessRecipe(org.bukkit.NamespacedKey, ItemStack)
     */
    @Nullable public static final Constructor<org.bukkit.inventory.ShapelessRecipe> SHAPELESS_RECIPE_CONSTRUCTOR = ReflectionUtility.getConstructor(10120, org.bukkit.inventory.ShapelessRecipe.class, RefNamespacedKey.NAMESPACED_KEY_CLASS, ItemStack.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefShapelessRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
