package xyz.srnyx.annoyingapi.stats;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Stat {
    /**
     * Defaults to the field name
     */
    String key() default "";

    /**
     * If this is a {@link Collection} or array, the size of the collection will be used as the value
     */
    boolean sizeOnly() default false;

    /**
     * If this is a {@link Map}, the keys of the map will be used as the value in a {@link List}
     */
    boolean mapKeysOnly() default false;
}
