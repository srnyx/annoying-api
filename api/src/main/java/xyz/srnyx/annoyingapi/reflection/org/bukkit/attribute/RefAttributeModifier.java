package xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute;

import org.bukkit.inventory.EquipmentSlot;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.util.UUID;


/**
 * org.bukkit.attribute.AttributeModifier
 */
public class RefAttributeModifier {
    /**
     * 1.9+ org.bukkit.attribute.AttributeModifier
     */
    @Nullable public static final Class<?> ATTRIBUTE_MODIFIER_CLASS = ReflectionUtility.getClass(10090, RefAttributeModifier.class);

    /**
     * 1.9+ org.bukkit.attribute.AttributeModifier(String, double, org.bukkit.attribute.AttributeModifier.Operation)
     */
    @Nullable public static final Constructor<?> ATTRIBUTE_MODIFIER_CONSTRUCTOR_3 = ReflectionUtility.getConstructor(10090, ATTRIBUTE_MODIFIER_CLASS, String.class, double.class, RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM);

    /**
     * 1.13.2+ org.bukkit.attribute.AttributeModifier(String, double, org.bukkit.attribute.AttributeModifier.Operation, org.bukkit.inventory.EquipmentSlot)
     */
    @Nullable public static final Constructor<?> ATTRIBUTE_MODIFIER_CONSTRUCTOR_5 = ReflectionUtility.getConstructor(10132, ATTRIBUTE_MODIFIER_CLASS, UUID.class, String.class, double.class, RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM, EquipmentSlot.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefAttributeModifier() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }

    /**
     * org.bukkit.attribute.AttributeModifier.Operation
     */
    public enum RefOperation {;
        /**
         * 1.9+ org.bukkit.attribute.AttributeModifier.Operation
         */
        @SuppressWarnings("rawtypes")
        @Nullable public static final Class<? extends Enum> ATTRIBUTE_MODIFIER_OPERATION_ENUM = ReflectionUtility.getEnum(10090, RefOperation.class);
    }
}
