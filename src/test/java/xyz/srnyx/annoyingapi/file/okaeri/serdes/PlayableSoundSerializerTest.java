package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import com.cryptomorin.xseries.XSound;
import eu.okaeri.configs.OkaeriConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import xyz.srnyx.annoyingapi.file.PlayableSound;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link PlayableSoundSerializer}.
 *
 * <p>XSeries lookups and {@link PlayableSound} construction do not require a real Bukkit server.
 * This test class uses the lightweight {@link ConfigTestSupport} proxy.
 */
public class PlayableSoundSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    public static class SoundConfig extends OkaeriConfig {
        public PlayableSound s = new PlayableSound(XSound.UI_BUTTON_CLICK, XSound.Category.MASTER, null, null);
    }

    private SoundConfig load(String yaml) throws IOException {
        final Path file = ConfigTestSupport.writeYaml(tempDir, "sound.yml", yaml);
        return new ConfigBuilder(new File(file.toString())).config(SoundConfig.class).build();
    }

    private static String yaml(String sound, String category) {
        return "s:\n  sound: " + sound + "\n  category: " + category;
    }

    // ------------------------------------------------------------------ Happy path

    @Test
    void basicRoundTrip_soundAndCategory() throws IOException {
        final SoundConfig config = load(yaml("ENTITY_PLAYER_LEVELUP", "MASTER"));

        assertNotNull(config.s);
        assertEquals(XSound.ENTITY_PLAYER_LEVELUP, config.s.sound);
        assertEquals(XSound.Category.MASTER, config.s.category);
    }

    @Test
    void volumeAndPitchPreserved() throws IOException {
        final SoundConfig config = load("s:\n  sound: ENTITY_PLAYER_LEVELUP\n  category: MASTER\n  volume: 0.5\n  pitch: 2.0");

        assertEquals(0.5f, config.s.volume, 0.001f);
        assertEquals(2.0f, config.s.pitch, 0.001f);
    }

    @Test
    void nullVolumeDefaultsTo1() throws IOException {
        final SoundConfig config = load(yaml("ENTITY_PLAYER_LEVELUP", "MASTER"));

        assertEquals(1.0f, config.s.volume, 0.001f);
    }

    @Test
    void nullPitchDefaultsTo1() throws IOException {
        final SoundConfig config = load(yaml("ENTITY_PLAYER_LEVELUP", "MASTER"));

        assertEquals(1.0f, config.s.pitch, 0.001f);
    }

    @Test
    void extremePitchValue_zero() throws IOException {
        final SoundConfig config = load("s:\n  sound: ENTITY_PLAYER_LEVELUP\n  category: MASTER\n  pitch: 0.0");

        assertEquals(0.0f, config.s.pitch, 0.001f);
    }

    @Test
    void highVolumeValue() throws IOException {
        final SoundConfig config = load("s:\n  sound: ENTITY_PLAYER_LEVELUP\n  category: MASTER\n  volume: 3.0");

        assertEquals(3.0f, config.s.volume, 0.001f);
    }

    @Test
    void blockSoundWithBlocksCategory() throws IOException {
        final SoundConfig config = load(yaml("BLOCK_ANVIL_LAND", "BLOCKS"));

        assertEquals(XSound.BLOCK_ANVIL_LAND, config.s.sound);
        assertEquals(XSound.Category.BLOCKS, config.s.category);
    }

    @Test
    void ambientCategory() throws IOException {
        final SoundConfig config = load(yaml("ENTITY_PLAYER_LEVELUP", "AMBIENT"));

        assertEquals(XSound.Category.AMBIENT, config.s.category);
    }

    // ------------------------------------------------------------------ Error paths

    @Test
    void invalidSoundName_throws() {
        assertThrows(Exception.class, () -> load(yaml("NOT_A_VALID_SOUND_XYZ", "MASTER")));
    }

    @Test
    void invalidCategoryName_throws() {
        assertThrows(Exception.class, () -> load(yaml("ENTITY_PLAYER_LEVELUP", "NOT_A_CATEGORY")));
    }

    @Test
    void missingSoundField_throws() {
        assertThrows(Exception.class, () -> load("s:\n  category: MASTER"));
    }

    @Test
    void missingCategoryField_throws() {
        assertThrows(Exception.class, () -> load("s:\n  sound: ENTITY_PLAYER_LEVELUP"));
    }

    // ------------------------------------------------------------------ All categories

    @ParameterizedTest(name = "category={0}")
    @EnumSource(XSound.Category.class)
    void allCategoryVariants_noException(XSound.Category category) throws IOException {
        assertDoesNotThrow(() -> load(yaml("ENTITY_PLAYER_LEVELUP", category.name())));
    }

    // ------------------------------------------------------------------ Serialization

    @Test
    void serializeRoundTrip_preservesSoundAndCategory() throws IOException {
        // saveDefaults writes the PlayableSound default (UI_BUTTON_CLICK / MASTER)
        final Path file = ConfigTestSupport.writeYaml(tempDir, "serial.yml", "");
        new ConfigBuilder(new File(file.toString()))
                .config(SoundConfig.class)
                .build();
        final String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains(XSound.UI_BUTTON_CLICK.name()), "Serialized file should contain sound name: " + content);
        assertTrue(content.contains(XSound.Category.MASTER.name()), "Serialized file should contain category: " + content);
        assertTrue(content.contains("volume:"), "Serialized file should contain 'volume:': " + content);
        assertTrue(content.contains("pitch:"), "Serialized file should contain 'pitch:': " + content);
    }

    @Test
    void serializeRoundTrip_volumeAndPitchPreservedInFile() throws IOException {
        // Load with specific volume/pitch, then check file
        load("s:\n  sound: ENTITY_PLAYER_LEVELUP\n  category: MASTER\n  volume: 0.75\n  pitch: 1.5");
        final Path file = ConfigTestSupport.writeYaml(tempDir, "serial2.yml",
                "s:\n  sound: ENTITY_PLAYER_LEVELUP\n  category: MASTER\n  volume: 0.75\n  pitch: 1.5");
        new ConfigBuilder(new File(file.toString())).config(SoundConfig.class).build();
        final String content = Files.readString(file, StandardCharsets.UTF_8);

        assertTrue(content.contains("0.75"), "File should preserve volume 0.75: " + content);
        assertTrue(content.contains("1.5"), "File should preserve pitch 1.5: " + content);
    }
}
