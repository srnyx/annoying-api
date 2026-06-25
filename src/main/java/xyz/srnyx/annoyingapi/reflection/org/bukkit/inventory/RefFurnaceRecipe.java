package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;


/**
 * org.bukkit.inventory.FurnaceRecipe
 */
public class RefFurnaceRecipe {
    /**
     * 1.9+ org.bukkit.inventory.FurnaceRecipe(org.bukkit.ItemStack, org.bukkit.material.MaterialData, float)
     */
    @Nullable public static Constructor<FurnaceRecipe> FURNACE_RECIPE_CONSTRUCTOR_1_9 = ReflectionUtility.getConstructor(1, 9, 0, FurnaceRecipe.class, ItemStack.class, MaterialData.class, float.class);

    /**
     * 1.13+ org.bukkit.inventory.FurnaceRecipe(org.bukkit.NamespacedKey, org.bukkit.inventory.ItemStack, Material, float, int)
     */
    @Nullable public static Constructor<FurnaceRecipe> FURNACE_RECIPE_CONSTRUCTOR_1_13 = ReflectionUtility.getConstructor(1, 13, 0, FurnaceRecipe.class, NAMESPACED_KEY_CLASS, ItemStack.class, Material.class, float.class, int.class);

    /**
     * 1.9+ org.bukkit.inventory.FurnaceRecipe#getExperience()
     */
    @Nullable public static Method FURNACE_RECIPE_GET_EXPERIENCE_METHOD = ReflectionUtility.getMethod(1, 9, 0, FurnaceRecipe.class, "getExperience");

    private RefFurnaceRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
