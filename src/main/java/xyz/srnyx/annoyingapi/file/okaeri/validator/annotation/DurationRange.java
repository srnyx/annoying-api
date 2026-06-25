package xyz.srnyx.annoyingapi.file.okaeri.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;


@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DurationRange {
    /**
     * Negative value means no minimum duration
     */
    long min() default -1;

    ChronoUnit minUnit() default ChronoUnit.MILLIS;

    /**
     * Negative value means no maximum duration
     */
    long max() default -1;

    ChronoUnit maxUnit() default ChronoUnit.MILLIS;
}
