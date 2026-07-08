package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.newNamespacedKey;


/**
 * 1.14+ org.bukkit.inventory.CookingRecipe
 */
public class RefCookingRecipe {
    /**
     * 1.14+ org.bukkit.inventory.CookingRecipe
     */
    @Nullable public static final Class<?> COOKING_RECIPE_CLASS = ReflectionUtility.getClass(1, 14, 0, RefCookingRecipe.class);

    /**
     * 1.14+ org.bukkit.inventory.CookingRecipe(org.bukkit.NamespacedKey, org.bukkit.inventory.ItemStack, org.bukkit.Material, float, int)
     */
    @Nullable public static final Constructor<?> COOKING_RECIPE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 14, 0, COOKING_RECIPE_CLASS, NAMESPACED_KEY_CLASS, ItemStack.class, Material.class, float.class, int.class);

    /**
     * 1.14+ org.bukkit.inventory.CookingRecipe#getCookingTime()
     */
    @Nullable public static final Method COOKING_RECIPE_GET_COOKING_TIME_METHOD = ReflectionUtility.getMethod(1, 14, 0, COOKING_RECIPE_CLASS, "getCookingTime");

    /**
     * 1.14+ org.bukkit.inventory.CookingRecipe#getExperience()
     */
    @Nullable public static final Method COOKING_RECIPE_GET_EXPERIENCE_METHOD = ReflectionUtility.getMethod(1, 14, 0, COOKING_RECIPE_CLASS, "getExperience");

    /**
     * 1.14+ org.bukkit.inventory.CookingRecipe#getInput()
     */
    @Nullable public static final Method COOKING_RECIPE_GET_INPUT = ReflectionUtility.getMethod(1, 14, 0, COOKING_RECIPE_CLASS, "getInput");

    @Nullable
    public static Recipe newCookingRecipe(@Nullable Object namespacedKey, @NotNull ItemStack result, @NotNull Material input, float experience, int cookingTime) {
        if (COOKING_RECIPE_CONSTRUCTOR == null || namespacedKey == null) return null;

        try {
            return (Recipe) COOKING_RECIPE_CONSTRUCTOR.newInstance(namespacedKey, result, input, experience, cookingTime);
        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Recipe newCookingRecipe(@NotNull Plugin plugin, @NotNull String key, @NotNull ItemStack result, @NotNull Material input, float experience, int cookingTime) {
        return newCookingRecipe(newNamespacedKey(plugin, key), result, input, experience, cookingTime);
    }

    private RefCookingRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
