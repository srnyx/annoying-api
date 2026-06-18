package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;


/**
 * 1.9+ org.bukkit.inventory.MerchantRecipe
 */
public class RefMerchantRecipe {
    /**
     * 1.9+ org.bukkit.inventory.MerchantRecipe
     */
    @Nullable public static final Class<?> MERCHANT_RECIPE_CLASS = ReflectionUtility.getClass(1, 9, 0, RefMerchantRecipe.class);

    /**
     * 1.9+ org.bukkit.inventory.MerchantRecipe(org.bukkit.ItemStack, int, int, boolean)
     */
    @Nullable public static final Constructor<?> MERCHANT_RECIPE_CONSTRUCTOR = ReflectionUtility.getConstructor(1, 9, 0, MERCHANT_RECIPE_CLASS, ItemStack.class, int.class, int.class, boolean.class);

    /**
     * 1.14+ org.bukkit.inventory.MerchantRecipe(org.bukkit.ItemStack, int, int, boolean, int, float)
     */
    @Nullable public static final Constructor<?> MERCHANT_RECIPE_CONSTRUCTOR_1_14 = ReflectionUtility.getConstructor(1, 14, 0, MERCHANT_RECIPE_CLASS, ItemStack.class, int.class, int.class, boolean.class, int.class, float.class);

    /**
     * 1.18.1+ org.bukkit.inventory.MerchantRecipe(org.bukkit.inventory.ItemStack, int, int, boolean, int, float, int, int)
     */
    @Nullable public static final Constructor<?> MERCHANT_RECIPE_CONSTRUCTOR_1_18_1 = ReflectionUtility.getConstructor(1, 18, 1, MERCHANT_RECIPE_CLASS, ItemStack.class, int.class, int.class, boolean.class, int.class, float.class, int.class, int.class);

    private RefMerchantRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
