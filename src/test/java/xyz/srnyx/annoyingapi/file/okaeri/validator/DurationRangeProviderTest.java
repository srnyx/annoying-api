package xyz.srnyx.annoyingapi.file.okaeri.validator;

import eu.okaeri.validator.ConstraintViolation;
import eu.okaeri.validator.exception.ValidatorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.DurationRange;
import xyz.srnyx.annoyingapi.file.okaeri.validator.provider.DurationRangeProvider;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;


class DurationRangeProviderTest {
    private final DurationRangeProvider provider = new DurationRangeProvider();

    // Holder class whose fields supply @DurationRange annotations
    @SuppressWarnings("unused")
    private static class Holder {
        @DurationRange(min = 5, minUnit = ChronoUnit.SECONDS)
        public Duration minOnly;
        @DurationRange(max = 60, maxUnit = ChronoUnit.SECONDS)
        public Duration maxOnly;
        @DurationRange(min = 5, minUnit = ChronoUnit.SECONDS, max = 60, maxUnit = ChronoUnit.SECONDS)
        public Duration bothBounds;
        @DurationRange(min = 500, minUnit = ChronoUnit.MILLIS)
        public Duration milliMin;
        @DurationRange(min = 1, minUnit = ChronoUnit.MINUTES)
        public Duration minuteMin;
        @DurationRange
        public Duration noBounds;
    }

    private static DurationRange anno(String field) {
        try {
            return Holder.class.getDeclaredField(field).getAnnotation(DurationRange.class);
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<ConstraintViolation> validate(DurationRange annotation, Object value) {
        return provider.validate(annotation, null, value, Duration.class, Duration.class, "testField");
    }

    @Test
    void nullValueReturnsEmpty() {
        assertTrue(validate(anno("minOnly"), null).isEmpty());
    }

    @Test
    void valueWithinRangeReturnsEmpty() {
        assertTrue(validate(anno("bothBounds"), Duration.ofSeconds(30)).isEmpty());
    }

    @Test
    void valueAtMinBoundReturnsEmpty() {
        assertTrue(validate(anno("minOnly"), Duration.ofSeconds(5)).isEmpty());
    }

    @Test
    void valueAtMaxBoundReturnsEmpty() {
        assertTrue(validate(anno("maxOnly"), Duration.ofSeconds(60)).isEmpty());
    }

    @Test
    void zeroDurationValidWhenNoBoundsSet() {
        assertTrue(validate(anno("noBounds"), Duration.ZERO).isEmpty());
    }

    @Test
    void nonDurationValueThrowsValidatorException() {
        assertThrows(ValidatorException.class, () -> validate(anno("minOnly"), "not a duration"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("outOfRangeCases")
    void outOfRangeReturnsOneViolation(String description, DurationRange annotation, Duration value, String expectedFragment) {
        final Set<ConstraintViolation> violations = validate(annotation, value);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains(expectedFragment),
                "Expected message to contain '" + expectedFragment + "'");
    }

    static Stream<Arguments> outOfRangeCases() {
        return Stream.of(
                Arguments.of("belowMin_greaterThanOrEqualTo",    anno("minOnly"),    Duration.ofSeconds(3),     "greater than or equal to"),
                Arguments.of("aboveMax_lessThanOrEqualTo",       anno("maxOnly"),    Duration.ofSeconds(90),    "less than or equal to"),
                Arguments.of("bothBounds_outOfRange_between",    anno("bothBounds"), Duration.ofSeconds(2),     "between"),
                Arguments.of("aboveMax_bothBounds_between",      anno("bothBounds"), Duration.ofSeconds(120),   "between"),
                Arguments.of("milliUnit_belowMin",               anno("milliMin"),   Duration.ofMillis(400),    "greater than or equal to"),
                Arguments.of("minuteUnit_belowMin",              anno("minuteMin"),  Duration.ofSeconds(30),    "greater than or equal to")
        );
    }

    @Test
    void minOnlyMessage_greaterThanOrEqualTo() {
        final Set<ConstraintViolation> violations = validate(anno("minOnly"), Duration.ofSeconds(1));
        assertFalse(violations.isEmpty());
        final String msg = violations.iterator().next().getMessage();
        assertTrue(msg.contains("greater than or equal to"), "Message: " + msg);
        assertFalse(msg.contains("between"), "Should not say 'between' for min-only: " + msg);
    }

    @Test
    void maxOnlyMessage_lessThanOrEqualTo() {
        final Set<ConstraintViolation> violations = validate(anno("maxOnly"), Duration.ofSeconds(90));
        assertFalse(violations.isEmpty());
        final String msg = violations.iterator().next().getMessage();
        assertTrue(msg.contains("less than or equal to"), "Message: " + msg);
        assertFalse(msg.contains("between"), "Should not say 'between' for max-only: " + msg);
    }
}
