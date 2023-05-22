package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.inventory.meta.Damageable
 */
public class Damageable {
    /**
     * 1.13+ org.bukkit.inventory.meta.Damageable
     */
    @Nullable public static final Class<?> DAMAGEABLE_CLASS = ReflectionUtility.getClass(10130, "org.bukkit.inventory.meta.Damageable");

    /**
     * 1.13+ org.bukkit.inventory.meta.Damageable#setDamage(int)
     */
    @Nullable public static final Method DAMAGEABLE_SET_DAMAGE_METHOD = ReflectionUtility.getMethod(10130, DAMAGEABLE_CLASS, "setDamage", int.class);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private Damageable() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
