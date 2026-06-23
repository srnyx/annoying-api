package xyz.srnyx.annoyingapi.file.okaeri.validator;

import eu.okaeri.validator.ConstraintViolation;
import eu.okaeri.validator.exception.ValidatorException;
import org.junit.jupiter.api.Test;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.PatternCollection;
import xyz.srnyx.annoyingapi.file.okaeri.validator.provider.PatternCollectionProvider;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class PatternCollectionProviderTest {
    private final PatternCollectionProvider provider = new PatternCollectionProvider();

    @SuppressWarnings("unused")
    private static class Holder {
        @PatternCollection("^[a-z]+$")
        public Set<String> lowercase;
        @PatternCollection(value = "^[a-z]+$", message = "{value} bad")
        public Set<String> customMessage;
        @PatternCollection(value = "^[a-z]+$", message = "'{value}' does not match '{pattern}'")
        public Set<String> bothPlaceholders;
        @PatternCollection("^[a-z][a-z0-9_-]{2,15}$")
        public Set<String> complex;
    }

    private static PatternCollection anno(String field) {
        try {
            return Holder.class.getDeclaredField(field).getAnnotation(PatternCollection.class);
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<ConstraintViolation> validate(PatternCollection annotation, Object value) {
        return provider.validate(annotation, null, value, List.class, List.class, "testField");
    }

    @Test
    void nullValueReturnsEmpty() {
        assertTrue(validate(anno("lowercase"), null).isEmpty());
    }

    @Test
    void emptyCollectionReturnsEmpty() {
        assertTrue(validate(anno("lowercase"), List.of()).isEmpty());
    }

    @Test
    void allMatchingStringsReturnsEmpty() {
        assertTrue(validate(anno("lowercase"), List.of("abc", "xyz")).isEmpty());
    }

    @Test
    void oneFailingStringReturnsOneViolation() {
        final Set<ConstraintViolation> v = validate(anno("lowercase"), List.of("abc", "ABC"));
        assertEquals(1, v.size());
    }

    @Test
    void allFailingStringsReturnsAllViolations() {
        final Set<ConstraintViolation> v = validate(anno("lowercase"), List.of("ABC", "DEF"));
        assertEquals(2, v.size());
    }

    @Test
    void allViolationsCollectedTogether() {
        final Set<ConstraintViolation> v = validate(anno("lowercase"), List.of("ABC", "123", "DEF"));
        assertEquals(3, v.size());
    }

    @Test
    void violationMessageContainsValuePlaceholder() {
        final Set<ConstraintViolation> v = validate(anno("customMessage"), List.of("BAD"));
        assertEquals(1, v.size());
        assertTrue(v.iterator().next().getMessage().contains("BAD bad"), "Message: " + v.iterator().next().getMessage());
    }

    @Test
    void violationMessageContainsPatternPlaceholder() {
        final Set<ConstraintViolation> v = validate(anno("bothPlaceholders"), List.of("BAD"));
        assertEquals(1, v.size());
        final String msg = v.iterator().next().getMessage();
        assertTrue(msg.contains("BAD"), "Should contain value: " + msg);
        assertTrue(msg.contains("^[a-z]+$"), "Should contain pattern: " + msg);
    }

    @Test
    void defaultMessageFormatApplied() {
        final Set<ConstraintViolation> v = validate(anno("lowercase"), List.of("BAD"));
        assertEquals(1, v.size());
        final String msg = v.iterator().next().getMessage();
        assertTrue(msg.contains("BAD"), "Should contain value: " + msg);
        assertTrue(msg.contains("must match pattern"), "Should contain 'must match pattern': " + msg);
    }

    @Test
    void nonCollectionValueThrowsValidatorException() {
        assertThrows(ValidatorException.class, () -> validate(anno("lowercase"), "not a collection"));
    }

    @Test
    void setIsValidCollectionInput() {
        assertTrue(validate(anno("lowercase"), Set.of("abc")).isEmpty());
    }

    @Test
    void complexPattern_allMatch() {
        assertTrue(validate(anno("complex"), List.of("mock_one", "mock-two", "mock3")).isEmpty());
    }

    @Test
    void complexPattern_tooShortFails() {
        final Set<ConstraintViolation> v = validate(anno("complex"), List.of("ab"));
        assertEquals(1, v.size());
    }

    @Test
    void integerInCollectionFailsPatternMatch() {
        // Non-String elements fail the instanceof String check in PatternCollectionProvider
        final Set<ConstraintViolation> v = validate(anno("lowercase"), List.of(42));
        assertEquals(1, v.size());
    }
}
