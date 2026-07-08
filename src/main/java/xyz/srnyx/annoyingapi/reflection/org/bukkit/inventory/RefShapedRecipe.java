package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.newNamespacedKeyThrow;
import static xyz.srnyx.annoyingapi.utility.ReflectionUtility.*;


/**
 * org.bukkit.inventory.ShapelessRecipe
 */
public class RefShapedRecipe {
    /**
     * 1.12+ org.bukkit.inventory.ShapedRecipe(org.bukkit.NamespacedKey, org.bukkit.inventory.ItemStack)
     */
    @Nullable public static final Constructor<ShapedRecipe> SHAPED_RECIPE_CONSTRUCTOR = getConstructor(1, 12, 0, ShapedRecipe.class, NAMESPACED_KEY_CLASS, ItemStack.class);

    @NotNull
    public static ShapedRecipe newShapedRecipe(@NotNull ItemStack result, @Nullable Plugin plugin, @Nullable String key) {
        // 1.12+
        if (plugin != null && key != null && SHAPED_RECIPE_CONSTRUCTOR != null) try {
            return SHAPED_RECIPE_CONSTRUCTOR.newInstance(newNamespacedKeyThrow(plugin, key), result);
        } catch (final Exception e) {
            AnnoyingPlugin.log(Level.WARNING, "Failed to create 1.12+ ShapedRecipe", e);
        }

        // 1.11.2-
        return new ShapedRecipe(result);
    }

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefShapedRecipe() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
