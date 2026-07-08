package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.ServerSoftware;
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
     * PAPER 1.16.5+ org.bukkit.inventory.MerchantRecipe(org.bukkit.inventory.ItemStack, int, int, boolean, int, float, boolean)
     */
    @Nullable public static final Constructor<?> MERCHANT_RECIPE_CONSTRUCTOR_PAPER_1_16_5 = ReflectionUtility.getConstructor(ServerSoftware.PAPER, 1, 16, 5, MERCHANT_RECIPE_CLASS, ItemStack.class, int.class, int.class, boolean.class, int.class, float.class, boolean.class);

    /**
     * 1.18.1+ org.bukkit.inventory.MerchantRecipe(org.bukkit.inventory.ItemStack, int, int, boolean, int, float, int, int)
     */
    @Nullable public static final Constructor<?> MERCHANT_RECIPE_CONSTRUCTOR_1_18_1 = ReflectionUtility.getConstructor(1, 18, 1, MERCHANT_RECIPE_CLASS, ItemStack.class, int.class, int.class, boolean.class, int.class, float.class, int.class, int.class);

    /**
     * PAPER 1.18.1+ org.bukkit.inventory.MerchantRecipe(org.bukkit.inventory.ItemStack, int, int, boolean, int, float, int, int, boolean)
     */
    @Nullable public static final Constructor<?> MERCHANT_RECIPE_CONSTRUCTOR_PAPER_1_18_1 = ReflectionUtility.getConstructor(ServerSoftware.PAPER, 1, 18, 1, MERCHANT_RECIPE_CLASS, ItemStack.class, int.class, int.class, boolean.class, int.class, float.class, int.class, int.class, boolean.class);

    @Nullable
    public static Recipe newMerchantRecipe(@NotNull ItemStack result, int uses, int maxUses, boolean experienceReward, @Nullable Integer villagerExperience, @Nullable Float priceMultiplier, @Nullable Integer demand, @Nullable Integer specialPrice, @Nullable Boolean ignoreDiscounts) {
        // 1.14+
        if (MERCHANT_RECIPE_CONSTRUCTOR_1_14 != null && villagerExperience != null && priceMultiplier != null) {
            // 1.18.1+
            if (MERCHANT_RECIPE_CONSTRUCTOR_1_18_1 != null && demand != null && specialPrice != null) {
                // PAPER 1.18.1+
                if (MERCHANT_RECIPE_CONSTRUCTOR_PAPER_1_18_1 != null && ignoreDiscounts != null) {
                    try {
                        return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR_PAPER_1_18_1.newInstance(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice, ignoreDiscounts);
                    } catch (final ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                }

                // 1.18.1+
                try {
                    return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR_1_18_1.newInstance(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, demand, specialPrice);
                } catch (final ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }

            // PAPER 1.16.5+
            if (MERCHANT_RECIPE_CONSTRUCTOR_PAPER_1_16_5 != null && ignoreDiscounts != null) {
                try {
                    return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR_PAPER_1_16_5.newInstance(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier, ignoreDiscounts);
                } catch (final ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }

            // 1.14-1.16.4
            try {
                return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR_1_14.newInstance(result, uses, maxUses, experienceReward, villagerExperience, priceMultiplier);
            } catch (final ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        // 1.9+
        if (MERCHANT_RECIPE_CONSTRUCTOR != null) try {
            return (Recipe) MERCHANT_RECIPE_CONSTRUCTOR.newInstance(result, uses, maxUses, experienceReward);
        } catch (final ReflectiveOperationException e) {
            e.printStackTrace();
        }

        // 1.8.8- (shouldn't happen)
        return null;
    }

    private RefMerchantRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
