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


/**
 * Utility class for managing reflected objects
 */
public class ReflectionUtility {
    // 1.12+
    /**
     * org.bukkit.NamespacedKey
     */
    @Nullable private static Class<?> namespacedKeyClass;
    /**
     * org.bukkit.NamespacedKey#NamespacedKey(Plugin, String)
     */
    @Nullable public static Constructor<?> namespacedKeyConstructor;
    /**
     * org.bukkit.inventory.ShapelessRecipe#ShapelessRecipe(NamespacedKey, ItemStack)
     */
    @Nullable public static Constructor<ShapelessRecipe> shapelessRecipeConstructor;
    /**
     * org.bukkit.inventory.ShapedRecipe#ShapedRecipe(NamespacedKey, ItemStack)
     */
    @Nullable public static Constructor<ShapedRecipe> shapedRecipeConstructor;

    // 1.13.2+
    /**
     * org.bukkit.attribute.AttributeModifier#AttributeModifier(UUID, String, double, AttributeModifier.Operation, EquipmentSlot)
     */
    @Nullable public static Constructor<AttributeModifier> attributeModifierConstructor;
    /**
     * org.bukkit.inventory.meta.ItemMeta#addAttributeModifier(Attribute, AttributeModifier)
     */
    @Nullable public static Method addAttributeModifierMethod;
    /**
     * org.bukkit.inventory.meta.ItemMeta#getCustomTagContainer(NamespacedKey, ItemTagType)
     */
    @Nullable public static Method getCtcMethod;
    /**
     * org.bukkit.inventory.meta.tags.ItemTagType#STRING
     */
    @Nullable public static Object ittStringClass;
    /**
     * org.bukkit.inventory.meta.tags.CustomItemTagContainer#getCustomTag(NamespacedKey, ItemTagType)
     */
    @Nullable public static Method ctcGetCustomTagMethod;
    /**
     * org.bukkit.inventory.meta.tags.CustomItemTagContainer#setCustomTag(NamespacedKey, ItemTagType, Object)
     */
    @Nullable public static Method ctcSetCustomTagMethod;
    /**
     * org.bukkit.inventory.meta.tags.CustomItemTagContainer#setCustomTag(NamespacedKey, ItemTagType, Object)
     */
    @Nullable public static Method ctcRemoveCustomTagMethod;

    // 1.14+
    /**
     * org.bukkit.inventory.meta.ItemMeta#setCustomModelData(Integer)
     */
    @Nullable public static Method setCustomModelDataMethod;
    /**
     * org.bukkit.inventory.meta.ItemMeta#getPersistentDataContainer()
     */
    @Nullable public static Method getPdcMethod;
    /**
     * org.bukkit.persistence.PersistentDataType#STRING
     */
    @Nullable public static Object pdtStringField;
    /**
     * org.bukkit.persistence.PersistentDataContainer#get(NamespacedKey, PersistentDataType)
     */
    @Nullable public static Method pdcGetMethod;
    /**
     * org.bukkit.persistence.PersistentDataContainer#set(NamespacedKey, PersistentDataType, Object)
     */
    @Nullable public static Method pdcSetMethod;
    /**
     * org.bukkit.persistence.PersistentDataContainer#remove(NamespacedKey)
     */
    @Nullable public static Method pdcRemoveMethod;

    static {
        init();
    }

    /**
     * Initializes the reflection utility. A method is used to allow use of guard clauses
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void init() {

        // 1.12+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10120) return;
        final Class<ItemStack> itemStackClass = ItemStack.class;
        try {
            namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
            namespacedKeyConstructor = namespacedKeyClass.getConstructor(Plugin.class, String.class);
            shapelessRecipeConstructor = ShapelessRecipe.class.getConstructor(namespacedKeyClass, itemStackClass);
            shapedRecipeConstructor = ShapedRecipe.class.getConstructor(namespacedKeyClass, itemStackClass);
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        // 1.13.2+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10132) return;
        final Class<ItemMeta> itemMetaClass = ItemMeta.class;
        try {
            attributeModifierConstructor = AttributeModifier.class.getConstructor(UUID.class, String.class, double.class, AttributeModifier.Operation.class, EquipmentSlot.class);
            addAttributeModifierMethod = itemMetaClass.getMethod("addAttributeModifier", Attribute.class, AttributeModifier.class);
            getCtcMethod = itemMetaClass.getMethod("getCustomTagContainer");
            final Class<?> itemTagTypeClass = Class.forName("org.bukkit.inventory.meta.tags.ItemTagType");
            ittStringClass = itemTagTypeClass.getField("STRING").get(itemTagTypeClass);
            final Class<?> customItemTagContainerClass = Class.forName("org.bukkit.inventory.meta.tags.CustomItemTagContainer");
            ctcGetCustomTagMethod = customItemTagContainerClass.getMethod("getCustomTag", namespacedKeyClass, itemTagTypeClass);
            ctcSetCustomTagMethod = customItemTagContainerClass.getMethod("setCustomTag", namespacedKeyClass, itemTagTypeClass, Object.class);
            ctcRemoveCustomTagMethod = customItemTagContainerClass.getMethod("removeCustomTag", namespacedKeyClass);
        } catch (final NoSuchMethodException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        // 1.14+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10140) return;
        try {
            setCustomModelDataMethod = itemMetaClass.getMethod("setCustomModelData", Integer.class);
            getPdcMethod = Class.forName("org.bukkit.persistence.PersistentDataHolder").getMethod("getPersistentDataContainer");
            final Class<?> persistentDataTypeClass = Class.forName("org.bukkit.persistence.PersistentDataType");
            pdtStringField = persistentDataTypeClass.getField("STRING").get(persistentDataTypeClass);
            final Class<?> persistentDataContainerClass = Class.forName("org.bukkit.persistence.PersistentDataContainer");
            pdcGetMethod = persistentDataContainerClass.getMethod("get", namespacedKeyClass, persistentDataTypeClass);
            pdcSetMethod = persistentDataContainerClass.getMethod("set", namespacedKeyClass, persistentDataTypeClass, Object.class);
            pdcRemoveMethod = persistentDataContainerClass.getMethod("remove", namespacedKeyClass);
        } catch (final NoSuchMethodException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
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
