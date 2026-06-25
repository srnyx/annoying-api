package xyz.srnyx.annoyingapi.file.okaeri.validator;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.exception.ValidationException;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.configs.validator.ConfigValidator;
import eu.okaeri.validator.ConstraintViolation;
import eu.okaeri.validator.OkaeriValidator;
import eu.okaeri.validator.policy.NullPolicy;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.validator.provider.DurationRangeProvider;
import xyz.srnyx.annoyingapi.file.okaeri.validator.provider.PatternCollectionProvider;

import java.util.Set;
import java.util.stream.Collectors;


public class AnnoyingConfigValidator implements ConfigValidator {
    @NotNull public final OkaeriValidator validator;

    public AnnoyingConfigValidator(boolean defaultNotNull) {
        validator = OkaeriValidator.of(defaultNotNull ? NullPolicy.NOT_NULL : NullPolicy.NULLABLE);
        validator.register(new DurationRangeProvider());
        validator.register(new PatternCollectionProvider());
    }

    public AnnoyingConfigValidator() {
        this(false);
    }

    @Override
    public boolean validateOnLoad() {
        return false;
    }

    @Override
    public boolean isValid(@NotNull Object entity) {
        // Check config type
        if (!(entity instanceof final OkaeriConfig config)) throw new IllegalArgumentException("OkaeriValidator can only validate OkaeriConfig instances, got: " + entity.getClass().getName());

        // Validate fields
        for (final FieldDeclaration field : config.getDeclaration().getFields()) validateField(entity, field);

        return true;
    }

    private void validateField(@NotNull Object entity, @NotNull FieldDeclaration field) {
        // Get violations
        final Set<ConstraintViolation> violations = validator.validatePropertyValue(entity.getClass(), field.getField(), field.getValue());
        if (violations.isEmpty()) return;

        // Throw ValidationException
        throw new ValidationException(field.getName() + " (" + field.getValue() + ") is invalid: " + violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", ")));
    }
}
