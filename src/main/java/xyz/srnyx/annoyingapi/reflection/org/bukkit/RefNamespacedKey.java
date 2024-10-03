package xyz.srnyx.annoyingapi.reflection.org.bukkit;

import org.bukkit.plugin.Plugin;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


/**
 * 1.12+ org.bukkit.NamespacedKey
 */
public class RefNamespacedKey {
    /**
     * 1.12+ org.bukkit.NamespacedKey
     */
    @Nullable public static final Class<?> NAMESPACED_KEY_CLASS = ReflectionUtility.getClass(1, 12, 0, RefNamespacedKey.class);
    /**
     * 1.12+ org.bukkit.NamespacedKey#minecraft(String)
     */
    @Nullable public static final Method MINECRAFT_METHOD = ReflectionUtility.getMethod(1, 12, 0, NAMESPACED_KEY_CLASS, "minecraft", String.class);
    /**
     * 1.12+ org.bukkit.NamespacedKey(String, String)
     */
    @Nullable public static final Constructor<?> NAMESPACED_KEY_CONSTRUCTOR_STRING = ReflectionUtility.getConstructor(1, 12, 0, NAMESPACED_KEY_CLASS, String.class, String.class);
    /**
     * 1.12+ org.bukkit.NamespacedKey(org.bukkit.plugin.Plugin, String)
     */
    @Nullable public static final Constructor<?> NAMESPACED_KEY_CONSTRUCTOR_PLUGIN = ReflectionUtility.getConstructor(1, 12, 0, NAMESPACED_KEY_CLASS, Plugin.class, String.class);
    /**
     * 1.12+ org.bukkit.NamespacedKey#getNamespace()
     */
    @Nullable public static final Method NAMESPACED_KEY_GET_NAMESPACE_METHOD = ReflectionUtility.getMethod(1, 12, 0, NAMESPACED_KEY_CLASS, "getNamespace");
    /**
     * 1.12+ org.bukkit.NamespacedKey#getKey()
     */
    @Nullable public static final Method NAMESPACED_KEY_GET_KEY_METHOD = ReflectionUtility.getMethod(1, 12, 0, NAMESPACED_KEY_CLASS, "getKey");

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefNamespacedKey() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
