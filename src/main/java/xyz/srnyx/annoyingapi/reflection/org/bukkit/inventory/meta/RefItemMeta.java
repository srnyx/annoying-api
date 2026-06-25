package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta;

import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttribute;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.components.RefCustomModelDataComponent.CUSTOM_MODEL_DATA_COMPONENT_CLASS;


/**
 * org.bukkit.inventory.meta.ItemMeta
 */
public class RefItemMeta {
    /**
     * 1.11+ org.bukkit.inventory.meta.ItemMeta#isUnbreakable()
     */
    @Nullable public static final Method ITEM_META_IS_UNBREAKABLE = ReflectionUtility.getMethod(1, 11, 0, ItemMeta.class, "isUnbreakable");

    /**
     * 1.11+ org.bukkit.inventory.meta.ItemMeta#setUnbreakable(boolean)
     */
    @Nullable public static final Method ITEM_META_SET_UNBREAKABLE = ReflectionUtility.getMethod(1, 11, 0, ItemMeta.class, "setUnbreakable", boolean.class);

    /**
     * 1.13.2+ org.bukkit.inventory.meta.ItemMeta#getAttributeModifiers()
     */
    @Nullable public static final Method ITEM_META_GET_ATTRIBUTE_MODIFIERS = ReflectionUtility.getMethod(1, 13, 2, ItemMeta.class, "getAttributeModifiers");

    /**
     * 1.13.2+ org.bukkit.inventory.meta.ItemMeta#addAttributeModifier(org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier)
     */
    @Nullable public static final Method ITEM_META_ADD_ATTRIBUTE_MODIFIER = ReflectionUtility.getMethod(1, 13, 2, ItemMeta.class, "addAttributeModifier", RefAttribute.ATTRIBUTE_ENUM, RefAttributeModifier.ATTRIBUTE_MODIFIER_CLASS);

    /**
     * 1.13.2+ org.bukkit.inventory.meta.ItemMeta#getCustomTagContainer()
     */
    @Nullable public static final Method ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD = ReflectionUtility.getMethod(1, 13, 2, ItemMeta.class, "getCustomTagContainer");

    /**
     * 1.14+ org.bukkit.inventory.meta.ItemMeta#hasCustomModelData()
     */
    @Nullable public static final Method ITEM_META_HAS_CUSTOM_MODEL_DATA = ReflectionUtility.getMethod(1, 14, 0, ItemMeta.class, "hasCustomModelData");

    /**
     * 1.14+ org.bukkit.inventory.meta.ItemMeta#getCustomModelData()
     */
    @Nullable public static final Method ITEM_META_GET_CUSTOM_MODEL_DATA = ReflectionUtility.getMethod(1, 14, 0, ItemMeta.class, "getCustomModelData");

    /**
     * 1.14+ org.bukkit.inventory.meta.ItemMeta#setCustomModelData(Integer)
     */
    @Nullable public static final Method ITEM_META_SET_CUSTOM_MODEL_DATA = ReflectionUtility.getMethod(1, 14, 0, ItemMeta.class, "setCustomModelData", Integer.class);

    /**
     * 1.21.4+ org.bukkit.inventory.meta.ItemMeta#getCustomModelDataComponent()
     */
    @Nullable public static final Method ITEM_META_GET_CUSTOM_MODEL_DATA_COMPONENT = ReflectionUtility.getMethod(1, 21, 4, ItemMeta.class, "getCustomModelDataComponent");

    /**
     * 1.21.4+ org.bukkit.inventory.meta.ItemMeta#setCustomModelDataComponent(org.bukkit.inventory.meta.components.CustomModelDataComponent)
     */
    @Nullable public static final Method ITEM_META_SET_CUSTOM_MODEL_DATA_COMPONENT = ReflectionUtility.getMethod(1, 21, 4, ItemMeta.class, "setCustomModelDataComponent", CUSTOM_MODEL_DATA_COMPONENT_CLASS);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefItemMeta() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
