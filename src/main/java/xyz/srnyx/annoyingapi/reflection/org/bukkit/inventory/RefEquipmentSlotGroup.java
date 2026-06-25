package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory;

import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * 1.20.5+ org.bukkit.inventory.EquipmentSlotGroup
 */
public class RefEquipmentSlotGroup {
    /**
     * 1.20.5+ org.bukkit.inventory.EquipmentSlotGroup
     */
    @Nullable public static final Class<?> EQUIPMENT_SLOT_GROUP_CLASS = ReflectionUtility.getClass(1, 20, 5, RefEquipmentSlotGroup.class);

    /**
     * 1.20.5+ org.bukkit.inventory.EquipmentSlotGroup#ANY
     */
    @Nullable public static final Object EQUIPMENT_SLOT_GROUP_ANY = ReflectionUtility.getStaticFieldValue(1, 20, 5, EQUIPMENT_SLOT_GROUP_CLASS, "ANY");

    /**
     * 1.20.5+ org.bukkit.inventory.EquipmentSlotGroup#getByName(String)
     */
    @Nullable public static final Method EQUIPMENT_SLOT_GROUP_GET_BY_NAME_METHOD = ReflectionUtility.getMethod(1, 20, 5, EQUIPMENT_SLOT_GROUP_CLASS, "getByName", String.class);

    private RefEquipmentSlotGroup() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
