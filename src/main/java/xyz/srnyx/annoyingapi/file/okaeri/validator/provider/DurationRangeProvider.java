package xyz.srnyx.annoyingapi.file.okaeri.validator.provider;

import eu.okaeri.validator.ConstraintViolation;
import eu.okaeri.validator.exception.ValidatorException;
import eu.okaeri.validator.provider.ValidationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.DurationRange;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;


public class DurationRangeProvider implements ValidationProvider<DurationRange> {
    @NotNull
    public Class<DurationRange> getAnnotation() {
        return DurationRange.class;
    }

    @NotNull
    public Set<ConstraintViolation> validate(@NotNull DurationRange annotation, @Nullable Object annotationSource, @Nullable Object value, @NotNull Class<?> type, @NotNull Type genericType, @NotNull String name) {
        type = extractType(type, genericType);
        value = extractValue(value, type, genericType);
        if (value == null) return Collections.emptySet();

        // Validate type
        if (!(value instanceof Duration duration)) throw new ValidatorException("@DurationMax is not applicable for " + type + " [" + name + "]");

        // Get min and max
        final Duration min = annotation.min() >= 0 ? Duration.of(annotation.min(), annotation.minUnit()) : null;
        final Duration max = annotation.max() >= 0 ? Duration.of(annotation.max(), annotation.maxUnit()) : null;

        // Valid
        if ((min == null || duration.compareTo(min) >= 0) && (max == null || duration.compareTo(max) <= 0)) return Collections.emptySet();

        // Invalid
        final String message;
        if (min != null && max != null) {
            message = "must be between " + min + " and " + max;
        } else if (min != null) {
            message = "must be greater than or equal to " + min;
        } else {
            message = "must be less than or equal to " + max;
        }
        return Set.of(new ConstraintViolation(name, message, getType()));
    }
}
