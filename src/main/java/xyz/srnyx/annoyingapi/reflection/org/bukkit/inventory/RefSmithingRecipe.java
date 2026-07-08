package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.newNamespacedKey;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefRecipeChoice.RECIPE_CHOICE_CLASS;


/**
 * 1.16.1+ org.bukkit.inventory.SmithingRecipe
 */
public class RefSmithingRecipe {
    /**
     * 1.16.1+ org.bukkit.inventory.SmithingRecipe
     */
    @Nullable public static final Class<?> SMITHING_RECIPE_CLASS = ReflectionUtility.getClass(1, 16, 1, RefSmithingRecipe.class);

    /**
     * 1.16.1+ org.bukkit.inventory.SmithingRecipe(org.bukkit.NamespacedKey, org.bukkit.inventory.ItemStack, org.bukkit.inventory.RecipeChoice, org.bukkit.inventory.RecipeChoice)
     */
    @Nullable public static final Constructor<?> SMITHING_RECIPE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 16, 1, SMITHING_RECIPE_CLASS, NAMESPACED_KEY_CLASS, ItemStack.class, RECIPE_CHOICE_CLASS, RECIPE_CHOICE_CLASS);

    @Nullable
    public static Recipe newSmithingRecipe(@Nullable Object namespacedKey, @NotNull ItemStack result, @NotNull Object base, @NotNull Object addition) {
        if (SMITHING_RECIPE_CONSTRUCTOR == null || namespacedKey == null) return null;

        try {
            return (Recipe) SMITHING_RECIPE_CONSTRUCTOR.newInstance(namespacedKey, result, base, addition);
        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static Recipe newSmithingRecipe(@NotNull Plugin plugin, @NotNull String key, @NotNull ItemStack result, @NotNull Object base, @NotNull Object addition) {
        return newSmithingRecipe(newNamespacedKey(plugin, key), result, base, addition);
    }

    private RefSmithingRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
