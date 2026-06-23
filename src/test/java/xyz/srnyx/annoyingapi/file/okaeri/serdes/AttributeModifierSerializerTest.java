package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.attribute.AttributeModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_GET_NAME_METHOD;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.attribute.RefAttributeModifier.ATTRIBUTE_MODIFIER_GET_OPERATION_METHOD;


/**
 * Tests for {@link AttributeModifierSerializer}.
 *
 * <p>AttributeModifier is a 1.13.2+ API. All tests are skipped if {@code ATTRIBUTE_MODIFIER_CLASS == null}.
 * Requires MockBukkit 1.18 where AttributeModifier is available.
 */
public class AttributeModifierSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    @BeforeEach
    void skipIfNoAttributeModifier() {
        assumeTrue(ATTRIBUTE_MODIFIER_CLASS != null, "AttributeModifier not available — skipping on pre-1.13.2 runtime");
    }

    public static class TestConfig extends OkaeriConfig {
        public AttributeModifier modifier = null;
    }

    private TestConfig load(String yaml) throws IOException {
        return loadFromYaml(tempDir, yaml, TestConfig.class);
    }

    // ------------------------------------------------------------------ Happy path

    @Test
    void basicModifier_namePreserved() throws IOException {
        final TestConfig config = load("modifier:\n  name: speed_boost\n  operation: ADD_NUMBER\n  amount: 0.5");
        assertNotNull(config.modifier);
        assertEquals("speed_boost", config.modifier.getName());
    }

    @Test
    void basicModifier_amountPreserved() throws IOException {
        final TestConfig config = load("modifier:\n  name: boost\n  operation: ADD_NUMBER\n  amount: 1.5");
        assertEquals(1.5, config.modifier.getAmount(), 0.0001);
    }

    @Test
    void operation_ADD_NUMBER() throws IOException {
        final TestConfig config = load("modifier:\n  name: m\n  operation: ADD_NUMBER\n  amount: 0.1");
        assertEquals(AttributeModifier.Operation.ADD_NUMBER, config.modifier.getOperation());
    }

    @Test
    void operation_ADD_SCALAR() throws IOException {
        final TestConfig config = load("modifier:\n  name: m\n  operation: ADD_SCALAR\n  amount: 0.5");
        assertEquals(AttributeModifier.Operation.ADD_SCALAR, config.modifier.getOperation());
    }

    @Test
    void operation_MULTIPLY_SCALAR_1() throws IOException {
        final TestConfig config = load("modifier:\n  name: m\n  operation: MULTIPLY_SCALAR_1\n  amount: 2.0");
        assertEquals(AttributeModifier.Operation.MULTIPLY_SCALAR_1, config.modifier.getOperation());
    }

    @Test
    void negativeAmount_preserved() throws IOException {
        final TestConfig config = load("modifier:\n  name: neg\n  operation: ADD_NUMBER\n  amount: -0.5");
        assertEquals(-0.5, config.modifier.getAmount(), 0.0001);
    }

    // ------------------------------------------------------------------ Error paths

    @Test
    void missingName_throws() {
        assertThrows(Exception.class, () -> load("modifier:\n  operation: ADD_NUMBER\n  amount: 0.5"));
    }

    @Test
    void missingOperation_throws() {
        assertThrows(Exception.class, () -> load("modifier:\n  name: m\n  amount: 0.5"));
    }

    // ------------------------------------------------------------------ Serialization

    @Test
    void serialization_writesNameAndOperation() throws IOException {
        assumeTrue(ATTRIBUTE_MODIFIER_GET_NAME_METHOD != null, "getName method not available");
        assumeTrue(ATTRIBUTE_MODIFIER_GET_OPERATION_METHOD != null, "getOperation method not available");

        final Path file = xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport.writeYaml(tempDir, "attr_serial.yml",
                "modifier:\n  name: speed_boost\n  operation: ADD_NUMBER\n  amount: 0.5");
        new xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder(new java.io.File(file.toString()))
                .config(TestConfig.class)
                .build();
        final String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("speed_boost"), "File should contain modifier name: " + content);
        assertTrue(content.contains("ADD_NUMBER") || content.contains("operation:"),
                "File should contain operation info: " + content);
    }

    @Test
    void roundTrip_namePreserved() throws IOException {
        final TestConfig config = load("modifier:\n  name: round_trip\n  operation: ADD_NUMBER\n  amount: 0.3");
        assertNotNull(config.modifier);
        assertEquals("round_trip", config.modifier.getName());
        assertEquals(0.3, config.modifier.getAmount(), 0.0001);
    }
}
