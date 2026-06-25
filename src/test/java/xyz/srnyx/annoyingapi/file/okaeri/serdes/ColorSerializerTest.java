package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.color.ColorFormat;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.color.ColorSerializer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.color.ColorSpec;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link ColorSerializer}.
 *
 * <p>Bukkit's {@link Color} is a plain Java data class — no server is needed.
 * This test class uses the lightweight {@link ConfigTestSupport} proxy.
 *
 * <p>Two inner test configs separate NAME-format from CUSTOM-format behavior.
 */
public class ColorSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    // Field name "c" to avoid shadowing the serializer's internal "color" key
    public static class NameFormatConfig extends OkaeriConfig {
        @ColorSpec(formats = ColorFormat.NAME)
        public Color c = Color.RED;
    }

    public static class CustomFormatConfig extends OkaeriConfig {
        @ColorSpec(formats = ColorFormat.CUSTOM)
        public Color c = Color.RED;
    }

    public static class AllFormatsConfig extends OkaeriConfig {
        // No @ColorSpec annotation → so all formats are used
        public Color c = Color.RED;
    }

    private <C extends OkaeriConfig> C load(String yaml, Class<C> cls) throws IOException {
        return loadConfig(tempDir, yaml, cls);
    }

    // ------------------------------------------------------------------ NAME format

    @Test
    void nameFormat_RED() throws IOException {
        // NAME format: YAML section has a "color" key with the color name
        final NameFormatConfig config = load("c: RED", NameFormatConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void nameFormat_BLUE() throws IOException {
        final NameFormatConfig config = load("c: BLUE", NameFormatConfig.class);

        assertEquals(Color.BLUE, config.c);
    }

    @Test
    void nameFormat_WHITE() throws IOException {
        final NameFormatConfig config = load("c: WHITE", NameFormatConfig.class);

        assertEquals(Color.WHITE, config.c);
    }

    @Test
    void nameFormat_BLACK() throws IOException {
        final NameFormatConfig config = load("c: BLACK", NameFormatConfig.class);

        assertEquals(Color.BLACK, config.c);
    }

    @Test
    void nameFormat_lowercaseNormalized() throws IOException {
        // toUpperCase() is applied before lookup
        final NameFormatConfig config = load("c: red", NameFormatConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void nameFormat_mixedCaseNormalized() throws IOException {
        final NameFormatConfig config = load("c: Blue", NameFormatConfig.class);

        assertEquals(Color.BLUE, config.c);
    }

    @Test
    void nameFormat_unknownName_returnsNull() throws IOException {
        // HashMap.get() returns null for unknown keys — no exception
        final NameFormatConfig config = load("c: NOT_A_REAL_COLOR", NameFormatConfig.class);

        assertNull(config.c, "Unknown color name should return null (not throw)");
    }

    @Test
    void nameFormat_serializeRoundTrip_RED() throws IOException {
        // saveDefaults writes the default (Color.RED) using NAME format
        final Path file = ConfigTestSupport.writeYaml(tempDir, "nameRound.yml", "");
        final String content = buildAndReadFile(file, NameFormatConfig.class);

        assertTrue(content.contains("RED"), "Serialized NAME format should contain 'RED': " + content);
    }

    // ------------------------------------------------------------------ CUSTOM format

    @Test
    void customFormat_integerRgb_red() throws IOException {
        // 0xFF0000 = 16711680
        final CustomFormatConfig config = load("c: 16711680", CustomFormatConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void customFormat_integerRgb_black() throws IOException {
        final CustomFormatConfig config = load("c: 0", CustomFormatConfig.class);

        assertEquals(Color.BLACK, config.c);
    }

    @Test
    void customFormat_hexStringWithHash() throws IOException {
        final CustomFormatConfig config = load("c: \"#FF0000\"", CustomFormatConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void customFormat_hexStringNoHash() throws IOException {
        final CustomFormatConfig config = load("c: \"FF0000\"", CustomFormatConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void customFormat_separateRgbFields_red() throws IOException {
        final CustomFormatConfig config = load("c:\n  red: 255\n  green: 0\n  blue: 0", CustomFormatConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void customFormat_separateRgbFields_allZero_black() throws IOException {
        final CustomFormatConfig config = load("c:\n  red: 0\n  green: 0\n  blue: 0", CustomFormatConfig.class);

        assertEquals(Color.BLACK, config.c);
    }

    @Test
    void customFormat_separateRgbFields_allMax_white() throws IOException {
        final CustomFormatConfig config = load("c:\n  red: 255\n  green: 255\n  blue: 255", CustomFormatConfig.class);

        assertEquals(Color.WHITE, config.c);
    }

    @Test
    void customFormat_emptySection_throws() {
        // No valid sub-keys and not a scalar → should throw
        assertThrows(Exception.class, () -> load("c:\n  unrelated: foo", CustomFormatConfig.class));
    }

    @Test
    void customFormat_serializeRoundTrip_writesRgb() throws IOException {
        // saveDefaults with default Color.RED (CUSTOM) → writes red/green/blue keys
        final Path file = ConfigTestSupport.writeYaml(tempDir, "customRound.yml", "");
        final String content = buildAndReadFile(file, CustomFormatConfig.class);

        assertTrue(content.contains("red:"), "CUSTOM format should serialize with 'red:' key: " + content);
        assertTrue(content.contains("green:"), "CUSTOM format should serialize with 'green:' key: " + content);
        assertTrue(content.contains("blue:"), "CUSTOM format should serialize with 'blue:' key: " + content);
    }

    @Test
    void customFormat_integerRgb_specificColor() throws IOException {
        // 0x123456 = 1193046
        final CustomFormatConfig config = load("c: 1193046", CustomFormatConfig.class);

        assertEquals(Color.fromRGB(0x12, 0x34, 0x56), config.c);
    }

    // ------------------------------------------------------------------ All formats

    @Test
    void allFormats_integerRgb_red() throws IOException {
        final AllFormatsConfig config = load("c: 16711680", AllFormatsConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void allFormats_hexStringWithHash() throws IOException {
        final AllFormatsConfig config = load("c: \"#FF0000\"", AllFormatsConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void allFormats_hexStringWithoutHash() throws IOException {
        final AllFormatsConfig config = load("c: \"FF0000\"", AllFormatsConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void allFormats_separateRgbFields() throws IOException {
        final AllFormatsConfig config = load("c:\n  red: 0\n  green: 0\n  blue: 255", AllFormatsConfig.class);

        assertEquals(Color.BLUE, config.c);
    }

    @Test
    void allFormats_serializeRoundTrip_writesCustomFormat() throws IOException {
        // CUSTOM is checked first in serialize(), so no @ColorSpec → writes red/green/blue, not a name string
        final Path file = ConfigTestSupport.writeYaml(tempDir, "allRound.yml", "");
        final String content = buildAndReadFile(file, AllFormatsConfig.class);

        assertTrue(content.contains("red:"), "All-formats should serialize as CUSTOM (red:): " + content);
        assertTrue(content.contains("green:"), "All-formats should serialize as CUSTOM (green:): " + content);
        assertTrue(content.contains("blue:"), "All-formats should serialize as CUSTOM (blue:): " + content);
    }

    @Test
    void allFormats_colorName_deserializesViaNameFallback() throws IOException {
        // int parse fails ("RED" not decimal) → hex parse fails ('R' not hex) → NAME fallback
        final AllFormatsConfig config = load("c: RED", AllFormatsConfig.class);

        assertEquals(Color.RED, config.c);
    }

    @Test
    void allFormats_colorName_caseInsensitive() throws IOException {
        final AllFormatsConfig config = load("c: blue", AllFormatsConfig.class);

        assertEquals(Color.BLUE, config.c);
    }
}
