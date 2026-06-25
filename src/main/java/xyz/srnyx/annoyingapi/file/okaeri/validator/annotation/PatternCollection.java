package xyz.srnyx.annoyingapi.file.okaeri.validator.annotation;

import org.intellij.lang.annotations.Language;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PatternCollection {
    @Language("RegExp")
    String value();

    String message() default "{value} must match pattern {pattern}";
}
