package xyz.srnyx.annoyingapi.file.okaeri.validator.provider;

import eu.okaeri.validator.ConstraintViolation;
import eu.okaeri.validator.exception.ValidatorException;
import eu.okaeri.validator.provider.ValidationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.PatternCollection;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class PatternCollectionProvider implements ValidationProvider<PatternCollection> {
    @NotNull
    public Class<PatternCollection> getAnnotation() {
        return PatternCollection.class;
    }

    @NotNull
    public Set<ConstraintViolation> validate(@NotNull PatternCollection annotation, @Nullable Object annotationSource, @Nullable Object value, @NotNull Class<?> type, @NotNull Type genericType, @NotNull String name) {
        type = extractType(type, genericType);
        value = extractValue(value, type, genericType);
        if (value == null) return Collections.emptySet();

        if (!(value instanceof Collection<?> collection)) throw new ValidatorException("@PatternCollection is not applicable for " + type + " [" + name + "]");

        // Get Pattern
        final String patternStr = annotation.value();
        final Pattern pattern = Pattern.compile(patternStr);
        if (collection.isEmpty()) return Collections.emptySet();

        // Validate values
        final Set<ConstraintViolation> violations = new LinkedHashSet<>();
        for (final Object object : collection) {
            if (!(object instanceof String string) || !pattern.matcher(string).matches()) {
                violations.add(new ConstraintViolation(name, annotation.message()
                        .replace("{value}", object.toString())
                        .replace("{pattern}", patternStr), getType()));
            }
        }
        return violations;
    }
}
