package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.tags;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * org.bukkit.inventory.meta.tags.ItemTagType
 */
public class RefItemTagType {
    /**
     * 1.13.2+ org.bukkit.inventory.meta.tags.ItemTagType
     */
    @Nullable public static final Class<?> ITEM_TAG_TYPE_CLASS = ReflectionUtility.getClass(10132, RefItemTagType.class);

    /**
     * 1.13.2+ org.bukkit.inventory.meta.tags.ItemTagType#STRING
     */
    @Nullable public static final Object ITEM_TAG_TYPE_STRING = ReflectionUtility.getStaticFieldValue(10132, ITEM_TAG_TYPE_CLASS, "STRING");

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefItemTagType() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
