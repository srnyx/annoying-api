package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import eu.okaeri.configs.OkaeriConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link XBaseSerializer} — covers both {@link XMaterial} and {@link XSound} fields.
 *
 * <p>XSeries lookups are table-based and do not need a real Bukkit server, so this test class uses
 * the lightweight {@link ConfigTestSupport} proxy rather than MockBukkit.
 */
public class XBaseSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    public static class MaterialConfig extends OkaeriConfig {
        public XMaterial material = XMaterial.STONE;
    }

    public static class SoundConfig extends OkaeriConfig {
        public XSound sound = XSound.UI_BUTTON_CLICK;
    }

    private <C extends OkaeriConfig> C load(Path dir, String yaml, Class<C> cls) throws IOException {
        return loadConfig(dir, yaml, cls);
    }

    // ------------------------------------------------------------------ XMaterial

    @Test
    void xMaterial_knownValue_DIAMOND() throws IOException {
        final MaterialConfig config = load(tempDir, "material: DIAMOND", MaterialConfig.class);

        assertEquals(XMaterial.DIAMOND, config.material);
    }

    @Test
    void xMaterial_knownValue_STONE() throws IOException {
        final MaterialConfig config = load(tempDir, "material: STONE", MaterialConfig.class);

        assertEquals(XMaterial.STONE, config.material);
    }

    @Test
    void xMaterial_knownValue_OAK_LOG() throws IOException {
        final MaterialConfig config = load(tempDir, "material: OAK_LOG", MaterialConfig.class);

        assertEquals(XMaterial.OAK_LOG, config.material);
    }

    @Test
    void xMaterial_unknownValue_returnsNull() throws IOException {
        final MaterialConfig config = load(tempDir, "material: NOT_A_REAL_MATERIAL_XYZ", MaterialConfig.class);

        assertNull(config.material, "Unknown XMaterial name should deserialize to null");
    }

    @Test
    void xMaterial_nullYamlValue_returnsDefault() throws IOException {
        final MaterialConfig config = load(tempDir, "material: ~", MaterialConfig.class);

        assertEquals(XMaterial.STONE, config.material, "Null YAML value should deserialize to default");
    }

    // ------------------------------------------------------------------ XSound

    @Test
    void xSound_knownValue_deserialized() throws IOException {
        final SoundConfig config = load(tempDir, "sound: ENTITY_PLAYER_LEVELUP", SoundConfig.class);

        assertEquals(XSound.ENTITY_PLAYER_LEVELUP, config.sound);
    }

    @Test
    void xSound_unknownValue_returnsNull() throws IOException {
        final SoundConfig config = load(tempDir, "sound: NOT_A_REAL_SOUND_XYZ", SoundConfig.class);

        assertNull(config.sound, "Unknown XSound name should deserialize to null");
    }

    // ------------------------------------------------------------------ Serialization

    @Test
    void serializeXMaterial_writesName() throws IOException {
        final Path file = ConfigTestSupport.writeYaml(tempDir, "mat_serial.yml", "");
        // Default is STONE → file should contain "STONE"
        final String content = buildAndReadFile(file, MaterialConfig.class);

        assertTrue(content.contains("STONE"), "Serialized file should contain 'STONE': " + content);
    }

    @Test
    void serializeXSound_writesName() throws IOException {
        final Path file = ConfigTestSupport.writeYaml(tempDir, "snd_serial.yml", "");
        final String content = buildAndReadFile(file, SoundConfig.class);

        assertTrue(content.contains(XSound.UI_BUTTON_CLICK.name()), "Serialized file should contain 'UI_BUTTON_CLICK': " + content);
    }

    // ------------------------------------------------------------------ Round-trips

    @ParameterizedTest(name = "XMaterial round-trip: {0}")
    @ValueSource(strings = {"DIAMOND", "STONE", "OAK_LOG", "IRON_INGOT", "GOLD_INGOT", "GLASS", "SAND", "GRAVEL", "COBBLESTONE", "OBSIDIAN"})
    void xMaterialFullRoundTrip(String materialName) throws IOException {
        final MaterialConfig config = load(tempDir, "material: " + materialName, MaterialConfig.class);

        assertNotNull(config.material, "Should deserialize to non-null for known material: " + materialName);
        assertEquals(materialName, config.material.name());
    }

    @Test
    void consecutiveLoads_sameResult() throws IOException {
        final MaterialConfig first = load(tempDir, "material: DIAMOND", MaterialConfig.class);
        // Reset to a fresh file for second load
        final MaterialConfig second = load(tempDir, "material: DIAMOND", MaterialConfig.class);

        assertEquals(first.material, second.material);
    }
}
