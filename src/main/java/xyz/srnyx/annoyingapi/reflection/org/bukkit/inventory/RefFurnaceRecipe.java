package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.newNamespacedKey;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.newNamespacedKeyThrow;


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

    @NotNull
    public static FurnaceRecipe newFurnaceRecipe(@NotNull ItemStack result, @NotNull Material input, @Nullable Float experience, @Nullable Plugin plugin, @Nullable String key, @Nullable Integer cookingTime) {
        // 1.9+
        if (experience != null) {
            // 1.13+
            if (FURNACE_RECIPE_CONSTRUCTOR_1_13 != null && plugin != null && key != null && cookingTime != null) try {
                return FURNACE_RECIPE_CONSTRUCTOR_1_13.newInstance(newNamespacedKeyThrow(plugin, key), result, input, experience, cookingTime);
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
            }

            // 1.9+
            if (FURNACE_RECIPE_CONSTRUCTOR_1_9 != null) try {
                return FURNACE_RECIPE_CONSTRUCTOR_1_9.newInstance(result, new MaterialData(input), experience);
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        // 1.8.8-
        return new FurnaceRecipe(result, input);
    }

    private RefFurnaceRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
