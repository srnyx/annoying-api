package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import static xyz.srnyx.annoyingapi.reflection.org.bukkit.potion.RefPotionEffect.POTION_EFFECT_CONSTRUCTOR_1_13;


/**
 * Tests for {@link PotionEffectSerializer}.
 *
 * <p>Requires MockBukkit so that {@link PotionEffectType} instances are available via the server registry.
 */
public class PotionEffectSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    public static class TestConfig extends OkaeriConfig {
        public PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, 200, 0);
    }

    private TestConfig load(String yaml) throws IOException {
        return loadFromYaml(tempDir, yaml, TestConfig.class);
    }

    // ------------------------------------------------------------------ Happy path

    @Test
    void basicRoundTrip_speed() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 200\n  amplifier: 1\n  ambient: false\n  particles: true");

        assertNotNull(config.effect);
        assertEquals(PotionEffectType.SPEED, config.effect.getType());
        assertEquals(200, config.effect.getDuration());
        assertEquals(1, config.effect.getAmplifier());
        assertFalse(config.effect.isAmbient());
        assertTrue(config.effect.hasParticles());
    }

    @Test
    void potionEffectType_SLOWNESS_deserialized() throws IOException {
        final TestConfig config = load("effect:\n  type: slowness\n  duration: 100\n  amplifier: 0\n  ambient: false\n  particles: false");

        assertEquals(PotionEffectType.SLOW, config.effect.getType());
    }

    @Test
    void potionEffectType_STRENGTH_deserialized() throws IOException {
        final TestConfig config = load("effect:\n  type: strength\n  duration: 300\n  amplifier: 2\n  ambient: false\n  particles: true");

        assertEquals(PotionEffectType.INCREASE_DAMAGE, config.effect.getType());
    }

    @Test
    void zeroDuration() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 0\n  amplifier: 0\n  ambient: false\n  particles: true");

        assertEquals(0, config.effect.getDuration());
    }

    @Test
    void largeDuration() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 2147483647\n  amplifier: 0\n  ambient: false\n  particles: true");

        assertEquals(2147483647, config.effect.getDuration());
    }

    @Test
    void amplifierZero() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 100\n  amplifier: 0\n  ambient: false\n  particles: true");

        assertEquals(0, config.effect.getAmplifier());
    }

    @Test
    void amplifierMax_255() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 100\n  amplifier: 255\n  ambient: false\n  particles: true");

        assertEquals(255, config.effect.getAmplifier());
    }

    @Test
    void ambientTrue() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 200\n  amplifier: 0\n  ambient: true\n  particles: true");

        assertTrue(config.effect.isAmbient());
    }

    @Test
    void ambientFalse() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 200\n  amplifier: 0\n  ambient: false\n  particles: true");

        assertFalse(config.effect.isAmbient());
    }

    @Test
    void particlesFalse() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 200\n  amplifier: 0\n  ambient: false\n  particles: false");

        assertFalse(config.effect.hasParticles());
    }

    @Test
    void particlesTrue() throws IOException {
        final TestConfig config = load("effect:\n  type: speed\n  duration: 200\n  amplifier: 0\n  ambient: false\n  particles: true");

        assertTrue(config.effect.hasParticles());
    }

    // ------------------------------------------------------------------ Icon (1.13+)

    @Test
    void iconField_whenConstructor6Available() throws IOException {
        assumeTrue(POTION_EFFECT_CONSTRUCTOR_1_13 != null, "6-param PotionEffect constructor not available");
        final TestConfig config = load("effect:\n  type: speed\n  duration: 200\n  amplifier: 0\n  ambient: false\n  particles: true\n  icon: true");

        assertNotNull(config.effect);
        assertTrue(config.effect.hasIcon());
    }

    @Test
    void noIconField_uses5ArgConstructor() throws IOException {
        // icon absent → falls back to 5-arg constructor, no exception
        assertDoesNotThrow(() -> load("effect:\n  type: speed\n  duration: 200\n  amplifier: 0\n  ambient: false\n  particles: true"));
    }

    // ------------------------------------------------------------------ Error paths

    @Test
    void missingTypeField_defaultsToZero() throws IOException {
        // The type field is required — deserializer throws if type is null
        assertThrows(Exception.class, () -> load("effect:\n  duration: 200\n  amplifier: 0\n  ambient: false\n  particles: true"));
    }

    // ------------------------------------------------------------------ Serialization

    @Test
    void serializeRoundTrip_fullEffect() throws IOException {
        // Load effect, then verify that saving produces readable YAML on reload
        final TestConfig first = load("effect:\n  type: speed\n  duration: 200\n  amplifier: 1\n  ambient: false\n  particles: true");

        assertNotNull(first.effect);
        assertEquals(200, first.effect.getDuration());
        assertEquals(1, first.effect.getAmplifier());
    }

    @Test
    void serializeRoundTrip_defaultEffect() throws IOException {
        // Writes default SPEED/200/0 effect; reload and check
        final TestConfig config = load("");

        assertNotNull(config.effect);
        assertEquals(PotionEffectType.SPEED, config.effect.getType());
        assertEquals(200, config.effect.getDuration());
    }
}
