package xyz.srnyx.annoyingapi.file.okaeri.serdes;

import eu.okaeri.configs.OkaeriConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;
import xyz.srnyx.annoyingapi.message.json.message.JsonTitleMessage;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link JsonTitleMessageSerializer}
 */
public class JsonTitleMessageSerializerTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    public static class TestConfig extends OkaeriConfig {
        public JsonTitleMessage title = null;
    }

    private TestConfig load(String yaml) throws IOException {
        return loadConfig(tempDir, yaml, TestConfig.class);
    }

    // ------------------------------------------------------------------ Deserialization

    @Test
    void basicDeserialize_titleAndSubtitle() throws IOException {
        final TestConfig config = load("title:\n  title: Hello\n  subtitle: World");
        assertNotNull(config.title);

        assertEquals("Hello", config.title.title);
        assertEquals("World", config.title.subtitle);
    }

    @Test
    void missingTitleDefaultsToEmpty() throws IOException {
        final TestConfig config = load("title:\n  subtitle: World");

        assertNotNull(config.title);
        assertEquals("", config.title.title);
    }

    @Test
    void missingSubtitleDefaultsToEmpty() throws IOException {
        final TestConfig config = load("title:\n  title: Hello");

        assertNotNull(config.title);
        assertEquals("", config.title.subtitle);
    }

    @Test
    void bothMissingDefaultToEmpty() throws IOException {
        // Empty section → both default to ""
        final TestConfig config = load("title:\n  unrelated: ignored");

        assertNotNull(config.title);
        assertEquals("", config.title.title);
        assertEquals("", config.title.subtitle);
    }

    @Test
    void emptyStringTitle_preserved() throws IOException {
        final TestConfig config = load("title:\n  title: \"\"\n  subtitle: Sub");

        assertNotNull(config.title);
        assertEquals("", config.title.title);
    }

    @Test
    void emptyStringSubtitle_preserved() throws IOException {
        final TestConfig config = load("title:\n  title: Title\n  subtitle: \"\"");

        assertNotNull(config.title);
        assertEquals("", config.title.subtitle);
    }

    @Test
    void colorCodesInTitle_preservedAsIs() throws IOException {
        // Serializer does not translate & codes — stored verbatim
        final TestConfig config = load("title:\n  title: \"&aGreen Title\"\n  subtitle: Sub");

        assertNotNull(config.title);
        assertEquals("&aGreen Title", config.title.title);
    }

    // ------------------------------------------------------------------ Serialization

    @Test
    void serializeWritesTitleAndSubtitleKeys() throws IOException {
        final Path path = ConfigTestSupport.writeYaml(tempDir, "title_serial.yml",
                "title:\n  title: T\n  subtitle: S");
        final String content = buildAndReadFile(path, TestConfig.class);

        assertTrue(content.contains("title:"), "File should contain 'title:' key: " + content);
        assertTrue(content.contains("subtitle:"), "File should contain 'subtitle:' key: " + content);
    }

    @Test
    void roundTrip_bothFields() throws IOException {
        final TestConfig config = load("title:\n  title: Round\n  subtitle: Trip");

        assertNotNull(config.title);
        assertEquals("Round", config.title.title);
        assertEquals("Trip", config.title.subtitle);
    }

    @Test
    void roundTrip_emptyFields() throws IOException {
        final TestConfig config = load("title:\n  title: \"\"\n  subtitle: \"\"");

        assertNotNull(config.title);
        assertEquals("", config.title.title);
        assertEquals("", config.title.subtitle);
    }
}
