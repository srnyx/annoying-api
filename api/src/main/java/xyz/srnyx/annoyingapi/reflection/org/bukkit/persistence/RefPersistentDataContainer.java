package xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.persistence.PersistentDataContainer
 */
public class RefPersistentDataContainer {
    /**
     * 1.14+ org.bukkit.persistence.PersistentDataContainer
     */
    @Nullable public static final Class<?> PERSISTENT_DATA_CONTAINER_CLASS = ReflectionUtility.getClass(10140, "org.bukkit.persistence.PersistentDataContainer");

    /**
     * 1.14+ org.bukkit.persistence.PersistentDataContainer#get(org.bukkit.NamespacedKey, org.bukkit.persistence.PersistentDataType)
     */
    @Nullable public static final Method PERSISTENT_DATA_CONTAINER_GET_METHOD = ReflectionUtility.getMethod(10140, PERSISTENT_DATA_CONTAINER_CLASS, "get", RefNamespacedKey.NAMESPACED_KEY_CLASS, RefPersistentDataType.PERSISTENT_DATA_TYPE_CLASS);

    /**
     * 1.14+ org.bukkit.persistence.PersistentDataContainer#get(org.bukkit.NamespacedKey, org.bukkit.persistence.PersistentDataType, Object)
     */
    @Nullable public static final Method PERSISTENT_DATA_CONTAINER_SET_METHOD = ReflectionUtility.getMethod(10140, PERSISTENT_DATA_CONTAINER_CLASS, "set", RefNamespacedKey.NAMESPACED_KEY_CLASS, RefPersistentDataType.PERSISTENT_DATA_TYPE_CLASS, Object.class);

    /**
     * 1.14+ org.bukkit.persistence.PersistentDataContainer#remove(org.bukkit.NamespacedKey)
     */
    @Nullable public static final Method PERSISTENT_DATA_CONTAINER_REMOVE_METHOD = ReflectionUtility.getMethod(10140, PERSISTENT_DATA_CONTAINER_CLASS, "remove", RefNamespacedKey.NAMESPACED_KEY_CLASS);

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefPersistentDataContainer() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
