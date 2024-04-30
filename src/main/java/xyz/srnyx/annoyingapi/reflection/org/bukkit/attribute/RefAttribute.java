package xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * org.bukkit.attribute.Attribute
 */
public enum RefAttribute {;
    /**
     * 1.9+ org.bukkit.attribute.Attribute
     */
    @SuppressWarnings("rawtypes")
    @Nullable public static final Class<? extends Enum> ATTRIBUTE_ENUM = ReflectionUtility.getEnum(1, 9, 0, RefAttribute.class);
}
