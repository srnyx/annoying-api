package xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;


/**
 * org.bukkit.persistence.PersistentDataHolder
 */
public class RefPersistentDataHolder {
    /**
     * 1.14+ org.bukkit.persistence.PersistentDataHolder
     */
    @Nullable public static final Class<?> PERSISTENT_DATA_HOLDER_CLASS = ReflectionUtility.getClass(10140, "org.bukkit.persistence.PersistentDataHolder");

    /**
     * 1.14+ org.bukkit.persistence.PersistentDataHolder#getPersistentDataContainer()
     */
    @Nullable public static final Method PERSISTENT_DATA_HOLDER_GET_PERSISTENT_DATA_CONTAINER_METHOD = ReflectionUtility.getMethod(10140, PERSISTENT_DATA_HOLDER_CLASS, "getPersistentDataContainer");

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefPersistentDataHolder() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
