package xyz.srnyx.annoyingapi.reflection.org.bukkit.persistence;

import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.utility.ReflectionUtility;


/**
 * org.bukkit.persistence.PersistentDataType
 */
public class RefPersistentDataType {
    /**
     * 1.14+ org.bukkit.persistence.PersistentDataType
     */
    @Nullable public static final Class<?> PERSISTENT_DATA_TYPE_CLASS = ReflectionUtility.getClass(1, 14, 0, RefPersistentDataType.class);

    /**
     * 1.14+ org.bukkit.persistence.PersistentDataType#STRING
     */
    @Nullable public static final Object PERSISTENT_DATA_TYPE_STRING = ReflectionUtility.getStaticFieldValue(1, 14, 0, PERSISTENT_DATA_TYPE_CLASS, "STRING");

    /**
     * 1.14+ org.bukkit.persistence.PersistentDataType#BYTE
     */
    @Nullable public static final Object PERSISTENT_DATA_TYPE_BYTE = ReflectionUtility.getStaticFieldValue(1, 14, 0, PERSISTENT_DATA_TYPE_CLASS, "BYTE");

    /**
     * This class cannot be instantiated
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private RefPersistentDataType() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
