package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;


/**
 * 1.13.1+ org.bukkit.inventory.RecipeChoice
 */
public class RefRecipeChoice {
    /**
     * 1.13.1+ org.bukkit.inventory.RecipeChoice
     */
    @Nullable public static final Class<?> RECIPE_CHOICE_CLASS = ReflectionUtility.getClass(1, 13, 1, RefRecipeChoice.class);

    private RefRecipeChoice() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }

    /**
     * 1.13.1+ org.bukkit.inventory.RecipeChoice.ExactChoice
     */
    public static class RefExactChoice {
        /**
         * 1.13.1+ org.bukkit.inventory.RecipeChoice.ExactChoice
         */
        @Nullable public static final Class<?> EXACT_CHOICE_CLASS = ReflectionUtility.getClass(1, 13, 1, RefExactChoice.class);

        /**
         * 1.13.1+ {@code org.bukkit.inventory.RecipeChoice.ExactChoice(List<org.bukkit.inventory.ItemStack>)}
         */
        @Nullable public static final Constructor<?> EXACT_CHOICE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 13, 1, EXACT_CHOICE_CLASS, List.class);

        /**
         * 1.13.1+ org.bukkit.inventory.RecipeChoice.ExactChoice#getChoices()
         */
        @Nullable public static final Method EXACT_CHOICE_GET_CHOICES_METHOD = ReflectionUtility.getMethod(1, 13, 1, EXACT_CHOICE_CLASS, "getChoices");

        private RefExactChoice() {
            throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
        }
    }

    /**
     * 1.13.1+ org.bukkit.inventory.RecipeChoice.MaterialChoice
     */
    public static class RefMaterialChoice {
        /**
         * 1.13.1+ org.bukkit.inventory.RecipeChoice.MaterialChoice
         */
        @Nullable public static final Class<?> MATERIAL_CHOICE_CLASS = ReflectionUtility.getClass(1, 13, 1, RefMaterialChoice.class);

        /**
         * 1.13.1+ {@code org.bukkit.inventory.RecipeChoice.MaterialChoice(List<org.bukkit.Material>)}
         */
        @Nullable public static final Constructor<?> MATERIAL_CHOICE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 13, 1, MATERIAL_CHOICE_CLASS, List.class);

        /**
         * 1.13.1+ org.bukkit.inventory.RecipeChoice.MaterialChoice#getChoices()
         */
        @Nullable public static final Method MATERIAL_CHOICE_GET_CHOICES_METHOD = ReflectionUtility.getMethod(1, 13, 1, MATERIAL_CHOICE_CLASS, "getChoices");

        private RefMaterialChoice() {
            throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
        }
    }
}
