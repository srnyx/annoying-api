package xyz.srnyx.annoyingapi.utility;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

import org.bukkit.entity.Player;
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
@SuppressWarnings("rawtypes")
public class ReflectionUtility {
    // 1.9+
    /**
     * org.bukkit.attribute.Attribute
     */
    @Nullable public static Class<? extends Enum> attributeEnum;
    /**
     * org.bukkit.attribute.AttributeModifier
     */
    @Nullable public static Class<?> attributeModifierClass;
    /**
     * org.bukkit.attribute.AttributeModifier.Operation
     */
    @Nullable public static Class<? extends Enum> attributeModifierOperationEnum;
    /**
     * org.bukkit.attribute.AttributeModifier#AttributeModifier(String, double, AttributeModifier.Operation)
     */
    @Nullable public static Constructor<?> attributeModifierConstructor3;

    // 1.11+
    /**
     * org.bukkit.inventory.meta.ItemMeta#setUnbreakable(boolean)
     */
    @Nullable public static Method setUnbreakableMethod;
    /**
     * org.bukkit.entity.Player.Spigot#sendMessage(ChatMessageType, BaseComponent...)
     */
    @Nullable public static Method sendMessageMethod;
    /**
     * org.bukkit.entity.Player#sendTitle(String, String, int, int, int)
     */
    @Nullable public static Method sendTitleMethod;

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

    // 1.13+
    /**
     * org.bukkit.inventory.meta.Damageable
     */
    @Nullable public static Class<?> damageableClass;
    /**
     * org.bukkit.inventory.meta.Damageable#setDamage(int)
     */
    @Nullable public static Method damageableSetDamageMethod;

    // 1.13.2+
    /**
     * org.bukkit.attribute.AttributeModifier#AttributeModifier(UUID, String, double, AttributeModifier.Operation, EquipmentSlot)
     */
    @Nullable public static Constructor<?> attributeModifierConstructor5;
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

    // 1.15+
    /**
     * net.md_5.bungee.ap.chat.ClickEvent.Action#COPY_TO_CLIPBOARD
     */
    @Nullable public static ClickEvent.Action clickEventActionCopyToClipboardEnum;

    static {
        init();
    }

    /**
     * Initializes the reflection utility. A method is used to allow use of guard clauses
     */
    @SuppressWarnings({"JavaReflectionMemberAccess", "unchecked"})
    private static void init() {
        // 1.9+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10090) return;
        try {
            attributeEnum = (Class<? extends Enum>) Class.forName("org.bukkit.attribute.Attribute");
            attributeModifierClass = Class.forName("org.bukkit.attribute.AttributeModifier");
            attributeModifierOperationEnum = (Class<? extends Enum>) Class.forName("org.bukkit.attribute.AttributeModifier$Operation");
            attributeModifierConstructor3 = attributeModifierClass.getConstructor(String.class, double.class, attributeModifierOperationEnum);
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        // 1.11+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10110) return;
        final Class<ItemMeta> itemMetaClass = ItemMeta.class;
        try {
            setUnbreakableMethod = itemMetaClass.getMethod("setUnbreakable", boolean.class);
            sendMessageMethod = Player.Spigot.class.getMethod("sendMessage", ChatMessageType.class, BaseComponent[].class);
            sendTitleMethod = Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        }

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

        // 1.13+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10130) return;
        try {
            damageableClass = Class.forName("org.bukkit.inventory.meta.Damageable");
            damageableSetDamageMethod = damageableClass.getMethod("setDamage", int.class);
        } catch (final ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        // 1.13.2+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10132) return;
        try {
            if (attributeModifierClass != null) attributeModifierConstructor5 = attributeModifierClass.getConstructor(UUID.class, String.class, double.class, attributeModifierOperationEnum, EquipmentSlot.class);
            addAttributeModifierMethod = itemMetaClass.getMethod("addAttributeModifier", attributeEnum, attributeModifierClass);
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

        // 1.15+
        if (AnnoyingPlugin.MINECRAFT_VERSION.value < 10150) return;
        try {
            clickEventActionCopyToClipboardEnum = ClickEvent.Action.valueOf("COPY_TO_CLIPBOARD");
        } catch (final IllegalArgumentException e) {
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
