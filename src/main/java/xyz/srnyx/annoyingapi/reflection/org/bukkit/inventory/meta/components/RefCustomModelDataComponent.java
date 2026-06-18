package xyz.srnyx.annoyingapi.reflection.org.bukkit.inventory.meta.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.utility.ReflectionUtility;

import java.lang.reflect.Method;
import java.util.List;


/**
 * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent
 */
public class RefCustomModelDataComponent {
    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent
     */
    @Nullable public static final Class<?> CUSTOM_MODEL_DATA_COMPONENT_CLASS = ReflectionUtility.getClass(1, 21, 4, RefCustomModelDataComponent.class);

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#getColors()
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_GET_COLORS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "getColors");

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#getFlags()
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_GET_FLAGS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "getFlags");

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#getFloats()
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_GET_FLOATS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "getFloats");

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#getStrings()
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_GET_STRINGS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "getStrings");

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#setColors(java.util.List)
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_SET_COLORS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "setColors", List.class);

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#setFlags(java.util.List)
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_SET_FLAGS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "setFlags", List.class);

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#setFloats(java.util.List)
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_SET_FLOATS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "setFloats", List.class);

    /**
     * 1.21.4+ org.bukkit.inventory.meta.components.CustomModelDataComponent#setStrings(java.util.List)
     */
    @Nullable public static final Method CUSTOM_MODEL_DATA_COMPONENT_SET_STRINGS_METHOD = ReflectionUtility.getMethod(1, 21, 4, CUSTOM_MODEL_DATA_COMPONENT_CLASS, "setStrings", List.class);

    public static boolean hasColors(@NotNull Object component) {
        if (CUSTOM_MODEL_DATA_COMPONENT_GET_COLORS_METHOD != null) try {
            return !((List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_COLORS_METHOD.invoke(component)).isEmpty();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasFlags(@NotNull Object component) {
        if (CUSTOM_MODEL_DATA_COMPONENT_GET_FLAGS_METHOD != null) try {
            return !((List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_FLAGS_METHOD.invoke(component)).isEmpty();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasFloats(@NotNull Object component) {
        if (CUSTOM_MODEL_DATA_COMPONENT_GET_FLOATS_METHOD != null) try {
            return !((List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_FLOATS_METHOD.invoke(component)).isEmpty();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasStrings(@NotNull Object component) {
        if (CUSTOM_MODEL_DATA_COMPONENT_GET_STRINGS_METHOD != null) try {
            return !((List<?>) CUSTOM_MODEL_DATA_COMPONENT_GET_STRINGS_METHOD.invoke(component)).isEmpty();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isEmpty(@NotNull Object component) {
        return !hasColors(component) && !hasFlags(component) && !hasFloats(component) && !hasStrings(component);
    }

    private RefCustomModelDataComponent() {
        throw new UnsupportedOperationException("This is a reflected class and cannot be instantiated");
    }
}
