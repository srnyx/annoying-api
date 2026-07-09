package xyz.srnyx.annoyingapi.stats;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;


@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Stat {
    /**
     * If this is a {@link Collection} or array, the size of the collection will be used as the value
     */
    boolean size() default false;
}
