package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Utility class for managing reflected objects
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ReflectionUtility {
    /**
     * Returns a {@link Class} if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   className       the class name
     *
     * @return                  the class if the version is greater than or equal to the minimum version, otherwise null
     */
    @Nullable
    public static Class<?> getClass(int minimumVersion, @NotNull String className) {
        if (checkVersion(minimumVersion)) return null;
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a {@link Class} from a reflection class if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   reflectionClass the reflection class
     *
     * @return                  the class if the version is greater than or equal to the minimum version, otherwise null
     */
    @Nullable
    public static Class<?> getClass(int minimumVersion, @NotNull Class<?> reflectionClass) {
        final String className = getClassName(reflectionClass);
        return className == null ? null : getClass(minimumVersion, className);
    }

    /**
     * Returns an {@link Enum} {@link Class} if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   enumClassName   the enum class name
     *
     * @return                  the enum if the version is greater than or equal to the minimum version, otherwise null
     */
    @Nullable
    public static Class<? extends Enum> getEnum(int minimumVersion, @NotNull String enumClassName) {
        if (checkVersion(minimumVersion)) return null;
        try {
            return (Class<? extends Enum>) Class.forName(enumClassName);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns an {@link Enum} {@link Class} from a reflection class if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   reflectionClass the reflection class
     *
     * @return                  the enum if the version is greater than or equal to the minimum version, otherwise null
     */
    @Nullable
    public static Class<? extends Enum> getEnum(int minimumVersion, @NotNull Class<?> reflectionClass) {
        final String enumClassName = getClassName(reflectionClass);
        return enumClassName == null ? null : getEnum(minimumVersion, enumClassName);
    }

    /**
     * Returns an array {@link Class} if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   className       the class name
     *
     * @return                  the array class if the version is greater than or equal to the minimum version, otherwise null
     */
    @Nullable
    public static Class<?> getClassArray(int minimumVersion, @NotNull String className) {
        if (checkVersion(minimumVersion)) return null;
        try {
            return Class.forName("[L" + className + ";");
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns an array {@link Class} from a reflection class if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   reflectionClass the reflection class
     *
     * @return                  the array class if the version is greater than or equal to the minimum version, otherwise null
     */
    @Nullable
    public static Class<?> getClassArray(int minimumVersion, @NotNull Class<?> reflectionClass) {
        final String className = getClassName(reflectionClass);
        return className == null ? null : getClassArray(minimumVersion, className);
    }

    /**
     * Returns a {@link Constructor} if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version, the class is not null, and none of the parameter types are null
     *
     * @param   minimumVersion  the minimum version
     * @param   clazz           the class to get the constructor from
     * @param   parameterTypes  the parameter types of the constructor
     *
     * @return                  the constructor if the version is greater than or equal to the minimum version, the class is not null, and none of the parameter types are null, otherwise null
     *
     * @param   <T>             the type of the class
     */
    @Nullable
    public static <T> Constructor<T> getConstructor(int minimumVersion, @Nullable Class<T> clazz, @Nullable Class<?>... parameterTypes) {
        if (clazz == null || parameterTypes == null || checkVersion(minimumVersion)) return null;
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a {@link Method} if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version, the class is not null, and none of the parameter types are null
     *
     * @param   minimumVersion  the minimum version
     * @param   clazz           the class to get the method from
     * @param   methodName      the name of the method
     * @param   parameterTypes  the parameter types of the method
     *
     * @return                  the method if the version is greater than or equal to the minimum version, the class is not null, and none of the parameter types are null, otherwise null
     *
     * @param   <T>             the type of the class
     */
    @Nullable
    public static <T> Method getMethod(int minimumVersion, @Nullable Class<T> clazz, @NotNull String methodName, @Nullable Class<?>... parameterTypes) {
        if (clazz == null || parameterTypes == null || checkVersion(minimumVersion)) return null;
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a {@link Field} if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   clazz           the class to get the field from
     * @param   fieldName       the name of the field
     *
     * @return                  the field if the version is greater than or equal to the minimum version, otherwise null
     */
    @Nullable
    public static Field getField(int minimumVersion, @Nullable Class<?> clazz, @NotNull String fieldName) {
        if (clazz == null || checkVersion(minimumVersion)) return null;
        try {
            return clazz.getField(fieldName);
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the value of a <b>static</b> {@link Field} if the result of {@link #getField(int, Class, String)} is not null
     *
     * @param   minimumVersion  the minimum version
     * @param   clazz           the class to get the field from
     * @param   fieldName       the name of the field
     *
     * @return                  the value of the field if the result of {@link #getField(int, Class, String)} is not null, otherwise null
     */
    @Nullable
    public static Object getStaticFieldValue(int minimumVersion, @Nullable Class<?> clazz, @NotNull String fieldName) {
        final Field field = getField(minimumVersion, clazz, fieldName);
        if (field == null) return null;
        try {
            return field.get(null);
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns an {@link Enum} value if {@link AnnoyingPlugin#MINECRAFT_VERSION} is greater than or equal to the minimum version
     *
     * @param   minimumVersion  the minimum version
     * @param   enumClass       the enum class
     * @param   enumName        the name of the enum
     *
     * @return                  the enum value if the version is greater than or equal to the minimum version, otherwise null
     *
     * @param   <T>             the type of the enum
     */
    @Nullable
    public static <T extends Enum> T getEnumValue(int minimumVersion, @Nullable Class<T> enumClass, @NotNull String enumName) {
        if (enumClass == null || checkVersion(minimumVersion)) return null;
        try {
            return (T) Enum.valueOf(enumClass, enumName);
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if {@link AnnoyingPlugin#MINECRAFT_VERSION} is less than the minimum version
     *
     * @param   minimumVersion  the minimum version
     *
     * @return                  true if the version is less than the minimum version, otherwise false
     */
    private static boolean checkVersion(int minimumVersion) {
    	return AnnoyingPlugin.MINECRAFT_VERSION.value < minimumVersion;
    }

    /**
     * Gets the reflection class name from a reflected class (must be in the {@code reflection} package)
     * <p><b>Example:</b> {@code xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey} -> {@code org.bukkit.NamespacedKey}</p>
     *
     * @param   reflectedClass  the reflected class
     *
     * @return                  the extracted class name if the class is in the {@code reflection} package, otherwise null
     */
    @Nullable
    private static String getClassName(@NotNull Class<?> reflectedClass) {
        final String[] split = reflectedClass.getName().split("\\.reflection\\.");
        return split.length == 1 ? null : split[1].replace("Ref", "");
    }

    /**
     * Constructs a new {@link ReflectionUtility} instance (illegal)
     *
     * @throws  UnsupportedOperationException   if this class is instantiated
     */
    private ReflectionUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
