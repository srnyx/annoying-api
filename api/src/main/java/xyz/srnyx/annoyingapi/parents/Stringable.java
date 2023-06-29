package xyz.srnyx.annoyingapi.parents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;


/**
 * Represents an object that can be converted to a {@link String} via {@link #toString()}
 */
public class Stringable {
    @Override @NotNull
    public String toString() {
        return toString(this);
    }

    /**
     * Creates a new {@link Stringable} object (shouldn't be used)
     */
    public Stringable() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Converts an object to a {@link String} by getting all of its fields and their values
     *
     * @param   object  the object to convert
     *
     * @return          the object as a {@link String}
     */
    @NotNull
    public static String toString( @Nullable Object object) {
        if (object == null) return "null";
        final Class<?> clazz = object.getClass();
        return clazz.getSimpleName() + "{" + Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> {
                    final String entry;
                    try {
                        final boolean inaccessible = !field.isAccessible();
                        if (inaccessible) field.setAccessible(true);
                        entry = field.getName() + "='" + field.get(object) + "'";
                        if (inaccessible) field.setAccessible(false);
                    } catch (final Exception e) {
                        return null;
                    }
                    return entry;
                })
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + ", " + b)
                .orElse("") + "}";
    }
}
