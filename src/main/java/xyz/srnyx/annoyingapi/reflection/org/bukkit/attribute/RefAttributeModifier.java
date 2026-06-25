package xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey;
import xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.RefEquipmentSlotGroup;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;


/**
 * org.bukkit.attribute.AttributeModifier
 */
public class RefAttributeModifier {
    /**
     * 1.9+ org.bukkit.attribute.AttributeModifier
     */
    @Nullable public static final Class<?> ATTRIBUTE_MODIFIER_CLASS = ReflectionUtility.getClass(1, 9, 0, RefAttributeModifier.class);

    /**
     * 1.9+ org.bukkit.attribute.AttributeModifier(String, double, org.bukkit.attribute.AttributeModifier.Operation)
     */
    @Nullable public static final Constructor<?> ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_9 = ReflectionUtility.getConstructor(1, 9, 0, ATTRIBUTE_MODIFIER_CLASS, String.class, double.class, RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM);

    /**
     * 1.13.2+ org.bukkit.attribute.AttributeModifier(String, double, org.bukkit.attribute.AttributeModifier.Operation, org.bukkit.inventory.EquipmentSlot)
     */
    @Nullable public static final Constructor<?> ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_13_2 = ReflectionUtility.getConstructor(1, 13, 2, ATTRIBUTE_MODIFIER_CLASS, UUID.class, String.class, double.class, RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM, EquipmentSlot.class);

    /**
     * 1.20.5+ org.bukkit.attribute.AttributeModifier(UUID, String, double, org.bukkit.attribute.AttributeModifier.Operation, org.bukkit.inventory.EquipmentSlot)
     */
    @Nullable public static final Constructor<?> ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_20_5 = ReflectionUtility.getConstructor(1, 20, 5, ATTRIBUTE_MODIFIER_CLASS, UUID.class, String.class, double.class, RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM, RefEquipmentSlotGroup.EQUIPMENT_SLOT_GROUP_CLASS);

    /**
     * 1.21+ org.bukkit.attribute.AttributeModifier(org.bukkit.NamespacedKey, double, org.bukkit.attribute.AttributeModifier.Operation, org.bukkit.inventory.EquipmentSlot)
     */
    @Nullable public static final Constructor<?> ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_21 = ReflectionUtility.getConstructor(1, 21, 0, ATTRIBUTE_MODIFIER_CLASS, RefNamespacedKey.NAMESPACED_KEY_CLASS, double.class, RefOperation.ATTRIBUTE_MODIFIER_OPERATION_ENUM, RefEquipmentSlotGroup.EQUIPMENT_SLOT_GROUP_CLASS);

    /**
     * 1.13.2+ org.bukkit.attribute.AttributeModifier#getName()
     */
    @Nullable public static final Method ATTRIBUTE_MODIFIER_GET_NAME_METHOD = ReflectionUtility.getMethod(1, 13, 2, ATTRIBUTE_MODIFIER_CLASS, "getName");

    /**
     * 1.13.2+ org.bukkit.attribute.AttributeModifier#getOperation()
     */
    @Nullable public static final Method ATTRIBUTE_MODIFIER_GET_OPERATION_METHOD = ReflectionUtility.getMethod(1, 13, 2, ATTRIBUTE_MODIFIER_CLASS, "getOperation");

    /**
     * 1.13.2+ org.bukkit.attribute.AttributeModifier#getSlot()
     */
    @Nullable public static final Method ATTRIBUTE_MODIFIER_GET_SLOT_METHOD = ReflectionUtility.getMethod(1, 13, 2, ATTRIBUTE_MODIFIER_CLASS, "getSlot");

    /**
     * 1.20.5+ org.bukkit.attribute.AttributeModifier#getSlotGroup()
     */
    @Nullable public static final Method ATTRIBUTE_MODIFIER_GET_SLOT_GROUP_METHOD = ReflectionUtility.getMethod(1, 20, 5, ATTRIBUTE_MODIFIER_CLASS, "getSlotGroup");

    /**
     * @param   operation   {@link RefOperation#ATTRIBUTE_MODIFIER_OPERATION_ENUM}
     * @param   slot        {@link EquipmentSlot} or {@link RefEquipmentSlotGroup#EQUIPMENT_SLOT_GROUP_CLASS}
     */
    @Nullable
    public static Object constructAttributeModifier(@Nullable Plugin plugin, @NotNull String name, double amount, @NotNull Object operation, @Nullable Object slot) {
        // 1.21+
        if (plugin != null && slot != null && ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_21 != null && NAMESPACED_KEY_CONSTRUCTOR != null) try {
            return ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_21.newInstance(NAMESPACED_KEY_CONSTRUCTOR.newInstance(plugin, name), amount, operation, slot);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // 1.20.5+
        if (ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_20_5 != null && slot != null) try {
            return ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_20_5.newInstance(UUID.randomUUID(), name, amount, operation, slot);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // 1.13.2+
        if (ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_13_2 != null && slot != null) try {
            return ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_13_2.newInstance(UUID.randomUUID(), name, amount, operation, slot);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // 1.9+
        if (ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_9 != null) try {
            return ATTRIBUTE_MODIFIER_CONSTRUCTOR_1_9.newInstance(name, amount, operation);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // Not supported
        return null;
    }

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
        @Nullable public static final Class<? extends Enum> ATTRIBUTE_MODIFIER_OPERATION_ENUM = ReflectionUtility.getEnum(1, 9, 0, RefOperation.class);
    }
}
