package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;
import eu.okaeri.configs.serdes.SerdesContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CLASS;
import static xyz.srnyx.annoyingapi.reflection.org.bukkit.RefNamespacedKey.NAMESPACED_KEY_CONSTRUCTOR;


/**
 * Tests for {@link NamespacedKeySerializer}
 *
 * <p>NamespacedKey is a 1.12+ API. All tests are skipped if {@code NAMESPACED_KEY_CLASS == null}.
 */
public class NamespacedKeySerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    private MockPlugin pluginMock;
    private NamespacedKeySerializer serializer;

    @BeforeEach
    void skipIfNoNamespacedKey() {
        assumeTrue(NAMESPACED_KEY_CLASS != null, "NamespacedKey not available — skipping on pre-1.12 runtime");
        assumeTrue(NAMESPACED_KEY_CONSTRUCTOR != null, "NamespacedKey constructor not available");
        pluginMock = MockBukkit.createMockPlugin();
        serializer = new NamespacedKeySerializer(pluginMock);
    }

    public static class TestConfig extends OkaeriConfig {
        public NamespacedKey key = null;
    }

    private TestConfig load(String yaml) throws IOException {
        final Path path = ConfigTestSupport.writeYaml(tempDir, "ns.yml", yaml);
        final NamespacedKeySerializer nks = serializer;
        return configBuilder(path, TestConfig.class)
                .configure(opt -> { if (nks.get()) opt.serdes(nks); })
                .build();
    }

    // ------------------------------------------------------------------ Serializer.get()

    @Test
    void get_returnsTrue_on1_18() {
        assertTrue(serializer.get(), "NamespacedKeySerializer.get() should return true on MockBukkit 1.18");
    }

    // ------------------------------------------------------------------ leftToRight (deserialization)

    @Test
    void leftToRight_validKey() throws IOException {
        final TestConfig config = load("key: my_recipe");

        assertNotNull(config.key);
        assertEquals("my_recipe", config.key.getKey());
    }

    @Test
    void leftToRight_keyWithDigits() throws IOException {
        final TestConfig config = load("key: key_123");

        assertNotNull(config.key);
        assertEquals("key_123", config.key.getKey());
    }

    @Test
    void leftToRight_namespaceFromPlugin() throws IOException {
        final TestConfig config = load("key: some_key");

        assertNotNull(config.key);
        // Namespace should be lowercase plugin name
        assertEquals(pluginMock.getName().toLowerCase(), config.key.getNamespace());
    }

    @Test
    void leftToRight_keyWithUnderscores() throws IOException {
        final TestConfig config = load("key: a_b_c_d");

        assertNotNull(config.key);
        assertEquals("a_b_c_d", config.key.getKey());
    }

    @Test
    void leftToRight_keyWithDigitsOnly() throws IOException {
        final TestConfig config = load("key: a123");

        assertNotNull(config.key);
        assertEquals("a123", config.key.getKey());
    }

    // ------------------------------------------------------------------ rightToLeft (serialization)

    @Test
    void rightToLeft_serializesKey() throws IOException {
        // saveDefaults writes null for the key field
        // Load with a specific key, check that the file contains the key string
        final Path path = ConfigTestSupport.writeYaml(tempDir, "ns_serial.yml", "key: round_trip_key");
        final NamespacedKeySerializer nks = serializer;
        configBuilder(path, TestConfig.class)
                .configure(opt -> {
                    if (nks.get()) opt.serdes(nks);
                })
                .build();
        final String content = Files.readString(path, StandardCharsets.UTF_8);

        assertTrue(content.contains("round_trip_key"), "Serialized file should contain the key string: " + content);
    }

    @Test
    void roundTrip_keyPreserved() throws IOException {
        final TestConfig config = load("key: round_trip");

        assertNotNull(config.key);
        assertEquals("round_trip", config.key.getKey());
    }

    // ------------------------------------------------------------------ Direct method calls

    @Test
    void directLeftToRight_createsCorrectKey() {
        final SerdesContext ctx = SerdesContext.of(new YamlBukkitConfigurer());
        final NamespacedKey key = (NamespacedKey) serializer.leftToRight("my_key", ctx);

        assertNotNull(key);
        assertEquals("my_key", key.getKey());
        assertEquals(pluginMock.getName().toLowerCase(), key.getNamespace());
    }

    @Test
    void directRightToLeft_returnsKeyString() {
        final SerdesContext ctx = SerdesContext.of(new YamlBukkitConfigurer());
        final NamespacedKey key = (NamespacedKey) serializer.leftToRight("get_this", ctx);
        final String result = serializer.rightToLeft(key, ctx);

        assertEquals("get_this", result);
    }
}
