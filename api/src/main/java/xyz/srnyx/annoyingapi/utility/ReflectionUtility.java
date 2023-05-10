package xyz.srnyx.annoyingapi.utility;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;


public class ReflectionUtility {
    // 1.12+
    @Nullable public static Constructor<?> namespacedKeyConstructor;
    @Nullable public static Constructor<ShapelessRecipe> shapelessRecipeConstructor;
    @Nullable public static Constructor<ShapedRecipe> shapedRecipeConstructor;

    // 1.13.2+
    @Nullable public static Constructor<AttributeModifier> attributeModifierConstructor;
    @Nullable public static Method addAttributeModifierMethod;

    // 1.14+
    @Nullable public static Method setCustomModelDataMethod;

    static {
        if (AnnoyingPlugin.MINECRAFT_VERSION.value >= 220) try {
            final Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
            namespacedKeyConstructor = namespacedKeyClass.getConstructor(Plugin.class, String.class);
            shapelessRecipeConstructor = ShapelessRecipe.class.getConstructor(namespacedKeyClass, ItemStack.class);
            shapedRecipeConstructor = ShapedRecipe.class.getConstructor(namespacedKeyClass, ItemStack.class);
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (AnnoyingPlugin.MINECRAFT_VERSION.value >= 232) try {
            attributeModifierConstructor = AttributeModifier.class.getConstructor(UUID.class, String.class, double.class, AttributeModifier.Operation.class, EquipmentSlot.class);
            addAttributeModifierMethod = ItemMeta.class.getMethod("addAttributeModifier", Attribute.class, AttributeModifier.class);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (AnnoyingPlugin.MINECRAFT_VERSION.value >= 240) try {
            setCustomModelDataMethod = ItemMeta.class.getMethod("setCustomModelData", Integer.class);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a new {@link ReflectionUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private ReflectionUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
