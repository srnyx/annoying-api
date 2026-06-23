package xyz.srnyx.annoyingapi.file.okaeri.serdes.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColorSpec {
    //TODO allow multiple
    ColorFormat format() default ColorFormat.CUSTOM;
}
