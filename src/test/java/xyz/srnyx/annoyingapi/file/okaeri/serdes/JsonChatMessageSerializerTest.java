package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;
import xyz.srnyx.annoyingapi.message.json.message.JsonChatMessage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Tests for {@link JsonChatMessageSerializer}.
 *
 * <p>Uses Mockito to create a minimal {@link AnnoyingPlugin} mock and registers the serializer
 * via the {@code configure()} callback since ConfigBuilder only registers it when plugin != null.
 */
public class JsonChatMessageSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    public static class TestConfig extends OkaeriConfig {
        public JsonChatMessage message = null;
    }

    private TestConfig load(String yaml) throws IOException {
        final AnnoyingPlugin mockPlugin = mock(AnnoyingPlugin.class);
        when(mockPlugin.getName()).thenReturn("test");
        final Path path = xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport.writeYaml(tempDir, "chat.yml", yaml);
        return (TestConfig) new ConfigBuilder(new File(path.toString()))
                .config(TestConfig.class)
                .configure(opt -> opt.serdes(new JsonChatMessageSerializer(mockPlugin)))
                .build();
    }

    // ------------------------------------------------------------------ Deserialization

    @Test
    void singleComponentScalarYaml_usesDefaultKey() throws IOException {
        final TestConfig config = load("message: \"Click here\"");
        assertNotNull(config.message);
        assertEquals(1, config.message.components.size());
        assertEquals("Click here", config.message.components.get("suggest_default"));
    }

    @Test
    void mapYaml_multipleComponentsPreserved() throws IOException {
        final TestConfig config = load("message:\n  suggest_run: /cmd\n  hover: Hover text");
        assertNotNull(config.message);
        assertEquals(2, config.message.components.size());
        assertEquals("/cmd", config.message.components.get("suggest_run"));
        assertEquals("Hover text", config.message.components.get("hover"));
    }

    @Test
    void emptyStringComponent_allowed() throws IOException {
        final TestConfig config = load("message: \"\"");
        assertNotNull(config.message);
        assertEquals("", config.message.components.get("suggest_default"));
    }

    @Test
    void singleComponent_withoutCommandPlaceholder_shouldCacheTrue() throws IOException {
        final TestConfig config = load("message: \"Hello world\"");
        assertNotNull(config.message);
        assertTrue(config.message.shouldCache());
    }

    @Test
    void singleComponent_withCommandPlaceholder_shouldCacheFalse() throws IOException {
        final TestConfig config = load("message: \"Run %command% to continue\"");
        assertNotNull(config.message);
        assertFalse(config.message.shouldCache());
    }

    @Test
    void multipleComponents_oneHasCommandPlaceholder_shouldCacheFalse() throws IOException {
        final TestConfig config = load("message:\n  hover: Plain text\n  suggest_run: /run %command%");
        assertNotNull(config.message);
        assertFalse(config.message.shouldCache());
    }

    @Test
    void multipleComponents_noneHasCommandPlaceholder_shouldCacheTrue() throws IOException {
        final TestConfig config = load("message:\n  hover: Hover\n  suggest_run: /run");
        assertNotNull(config.message);
        assertTrue(config.message.shouldCache());
    }

    // ------------------------------------------------------------------ Serialization

    @Test
    void singleComponentSerialized_asScalarInFile() throws IOException {
        final Path path = xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport.writeYaml(tempDir, "chat_serial.yml", "message: \"Hello\"");
        final AnnoyingPlugin mockPlugin = mock(AnnoyingPlugin.class);
        when(mockPlugin.getName()).thenReturn("test");
        new ConfigBuilder(new File(path.toString()))
                .config(TestConfig.class)
                .configure(opt -> opt.serdes(new JsonChatMessageSerializer(mockPlugin)))
                .build();
        final String content = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(content.contains("Hello"), "Scalar component should appear in file: " + content);
    }

    @Test
    void multipleComponentsSerialized_keysInFile() throws IOException {
        final Path path = xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport.writeYaml(tempDir, "chat_serial2.yml",
                "message:\n  suggest_run: /cmd\n  hover: Hover");
        final AnnoyingPlugin mockPlugin = mock(AnnoyingPlugin.class);
        when(mockPlugin.getName()).thenReturn("test");
        new ConfigBuilder(new File(path.toString()))
                .config(TestConfig.class)
                .configure(opt -> opt.serdes(new JsonChatMessageSerializer(mockPlugin)))
                .build();
        final String content = Files.readString(path, StandardCharsets.UTF_8);
        assertTrue(content.contains("suggest_run:"), "Map key 'suggest_run' should appear in file: " + content);
        assertTrue(content.contains("hover:"), "Map key 'hover' should appear in file: " + content);
    }

    // ------------------------------------------------------------------ Round-trip

    @Test
    void roundTrip_singleComponent() throws IOException {
        final TestConfig first = load("message: \"Round trip\"");
        assertNotNull(first.message);
        assertEquals("Round trip", first.message.components.get("suggest_default"));
    }

    @Test
    void roundTrip_multipleComponents() throws IOException {
        final TestConfig config = load("message:\n  a: alpha\n  b: beta\n  c: gamma");
        assertNotNull(config.message);
        assertEquals("alpha", config.message.components.get("a"));
        assertEquals("beta", config.message.components.get("b"));
        assertEquals("gamma", config.message.components.get("c"));
    }
}
