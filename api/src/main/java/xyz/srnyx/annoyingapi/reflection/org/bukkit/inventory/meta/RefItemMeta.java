package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta;

import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttribute;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.inventory.meta.ItemMeta
 */
public class RefItemMeta {
    /**
     * 1.11+ org.bukkit.inventory.meta.ItemMeta#setUnbreakable(boolean)
     */
    @Nullable public static final Method ITEM_META_SET_UNBREAKABLE = ReflectionUtility.getMethod(10110, ItemMeta.class, "setUnbreakable", boolean.class);

    /**
     * 1.13.2+ org.bukkit.inventory.meta.ItemMeta#addAttributeModifier(org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier)
     */
    @Nullable public static final Method ITEM_META_ADD_ATTRIBUTE_MODIFIER = ReflectionUtility.getMethod(10132, ItemMeta.class, "addAttributeModifier", RefAttribute.ATTRIBUTE_ENUM, RefAttributeModifier.ATTRIBUTE_MODIFIER_CLASS);

    /**
     * 1.13.2+ org.bukkit.inventory.meta.ItemMeta#getCustomTagContainer()
     */
    @Nullable public static final Method ITEM_META_GET_CUSTOM_TAG_CONTAINER_METHOD = ReflectionUtility.getMethod(10132, ItemMeta.class, "getCustomTagContainer");

    /**
     * 1.14+ org.bukkit.inventory.meta.ItemMeta#setCustomModelData(Integer)
     */
    @Nullable public static final Method ITEM_META_SET_CUSTOM_MODEL_DATA = ReflectionUtility.getMethod(10140, ItemMeta.class, "setCustomModelData", Integer.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefItemMeta() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
