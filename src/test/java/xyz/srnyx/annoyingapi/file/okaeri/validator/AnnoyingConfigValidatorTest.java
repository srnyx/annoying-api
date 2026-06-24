package xyz.srnyx.annoyingapi.file.okaeri.validator;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.exception.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.DurationRange;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.PatternCollection;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class AnnoyingConfigValidatorTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    // --- minimal test configs ---

    public static class DurationConfig extends OkaeriConfig {
        @DurationRange(min = 5, minUnit = ChronoUnit.SECONDS)
        public Duration interval = Duration.ofSeconds(30);
    }

    public static class DurationMaxConfig extends OkaeriConfig {
        @DurationRange(max = 60, maxUnit = ChronoUnit.SECONDS)
        public Duration interval = Duration.ofSeconds(30);
    }

    public static class PatternConfig extends OkaeriConfig {
        @PatternCollection("^[a-z]+$")
        public Set<String> tokens = new HashSet<>(Set.of("abc"));
    }

    // --- tests ---

    @Test
    void validateOnLoadReturnsFalse() {
        assertFalse(new AnnoyingConfigValidator().validateOnLoad());
    }

    @Test
    void defaultConstructorCreatesNullablePolicy() {
        assertNotNull(new AnnoyingConfigValidator().validator);
    }

    @Test
    void notNullConstructorCreatesValidator() {
        assertNotNull(new AnnoyingConfigValidator(true).validator);
    }

    @Test
    void nonOkaeriConfigInputThrowsIllegalArgument() {
        final AnnoyingConfigValidator validator = new AnnoyingConfigValidator();
        assertThrows(IllegalArgumentException.class, () -> validator.isValid(new Object()));
    }

    @Test
    void nonOkaeriConfigExceptionMessageContainsClassName() {
        final AnnoyingConfigValidator validator = new AnnoyingConfigValidator();
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.isValid("not a config"));
        assertTrue(ex.getMessage().contains("String"), "Message: " + ex.getMessage());
    }

    @Test
    void validDurationPassesIsValid() throws IOException {
        final DurationConfig config = loadConfig(tempDir, "interval: PT30S", DurationConfig.class);
        final AnnoyingConfigValidator validator = new AnnoyingConfigValidator();
        assertTrue(validator.isValid(config));
    }

    @Test
    void durationBelowMinThrowsValidationException() {
        assertThrows(ValidationException.class, () -> loadConfig(tempDir, "interval: PT3S", DurationConfig.class));
    }

    @Test
    void durationAboveMaxThrowsValidationException() {
        assertThrows(ValidationException.class, () -> loadConfig(tempDir, "interval: PT2M", DurationMaxConfig.class));
    }

    @Test
    void validationExceptionMessageContainsFieldName() {
        final ValidationException ex = assertThrows(ValidationException.class, () -> loadConfig(tempDir, "interval: PT3S", DurationConfig.class));
        assertTrue(ex.getMessage().contains("interval"), "Message: " + ex.getMessage());
    }

    @Test
    void validationExceptionMessageContainsViolatingValue() {
        final ValidationException ex = assertThrows(ValidationException.class, () -> loadConfig(tempDir, "interval: PT3S", DurationConfig.class));
        assertTrue(ex.getMessage().contains("PT3S"), "Message: " + ex.getMessage());
    }

    @Test
    void validPatternCollectionPassesIsValid() throws IOException {
        final PatternConfig config = loadConfig(tempDir, "tokens:\n  - abc\n  - xyz", PatternConfig.class);
        final AnnoyingConfigValidator validator = new AnnoyingConfigValidator();

        assertTrue(validator.isValid(config));
    }

    @Test
    void patternCollectionViolationThrowsValidationException() {
        assertThrows(ValidationException.class, () -> loadConfig(tempDir, "tokens:\n  - ABC", PatternConfig.class));
    }

    @Test
    void durationAtExactMinBoundPasses() throws IOException {
        final DurationConfig config = loadConfig(tempDir, "interval: PT5S", DurationConfig.class);
        final AnnoyingConfigValidator validator = new AnnoyingConfigValidator();

        assertTrue(validator.isValid(config));
    }
}
