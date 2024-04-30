package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.inventory.meta.Damageable
 */
public class RefDamageable {
    /**
     * 1.13+ org.bukkit.inventory.meta.Damageable
     */
    @Nullable public static final Class<?> DAMAGEABLE_CLASS = ReflectionUtility.getClass(1, 13, 0, RefDamageable.class);

    /**
     * 1.13+ org.bukkit.inventory.meta.Damageable#setDamage(int)
     */
    @Nullable public static final Method DAMAGEABLE_SET_DAMAGE_METHOD = ReflectionUtility.getMethod(1, 13, 0, DAMAGEABLE_CLASS, "setDamage", int.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefDamageable() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
