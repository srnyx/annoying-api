package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.tags;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.org.bukkit.NamespacedKey;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.inventory.meta.tags.CustomItemTagContainer
 */
public class CustomItemTagContainer {
    /**
     * 1.13.2+ org.bukkit.inventory.meta.tags.CustomItemTagContainer
     */
    @Nullable public static final Class<?> CUSTOM_ITEM_TAG_CONTAINER_CLASS = ReflectionUtility.getClass(10132, "org.bukkit.inventory.meta.tags.CustomItemTagContainer");

    /**
     * 1.13.2+ org.bukkit.inventory.meta.tags.CustomItemTagContainer#getCustomTag(org.bukkit.NamespacedKey, org.bukkit.inventory.meta.tags.ItemTagType)
     */
    @Nullable public static final Method CUSTOM_ITEM_TAG_CONTAINER_GET_CUSTOM_TAG_METHOD = ReflectionUtility.getMethod(10132, CUSTOM_ITEM_TAG_CONTAINER_CLASS, "getCustomTag", NamespacedKey.NAMESPACED_KEY_CLASS, ItemTagType.ITEM_TAG_TYPE_CLASS);

    /**
     * 1.13.2+ org.bukkit.inventory.meta.tags.CustomItemTagContainer#setCustomTag(org.bukkit.NamespacedKey, org.bukkit.inventory.meta.tags.ItemTagType, Object)
     */
    @Nullable public static final Method CUSTOM_ITEM_TAG_CONTAINER_SET_CUSTOM_TAG_METHOD = ReflectionUtility.getMethod(10132, CUSTOM_ITEM_TAG_CONTAINER_CLASS, "setCustomTag", NamespacedKey.NAMESPACED_KEY_CLASS, ItemTagType.ITEM_TAG_TYPE_CLASS, Object.class);

    /**
     * 1.13.2+ org.bukkit.inventory.meta.tags.CustomItemTagContainer#removeCustomTag(org.bukkit.NamespacedKey)
     */
    @Nullable public static final Method CUSTOM_ITEM_TAG_CONTAINER_REMOVE_CUSTOM_TAG_METHOD = ReflectionUtility.getMethod(10132, CUSTOM_ITEM_TAG_CONTAINER_CLASS, "removeCustomTag", NamespacedKey.NAMESPACED_KEY_CLASS);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private CustomItemTagContainer() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
