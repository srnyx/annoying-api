package xyz.srnyx.annoyingapi.file.okaeri.serdes.duration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;


/**
 * Add this to {@link Duration} fields that should fallback to Minecraft ticks if the value is a single number
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DurationTickFallback {}
