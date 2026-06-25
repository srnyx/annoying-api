package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.OkaeriConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Focused tests for the A0001 kebab→snake_case migration.
 *
 * <p>The migration is registered automatically by ConfigBuilder (renameKebabCaseToSnakeCase=true).
 * We verify the YAML file content after loading to confirm keys were renamed.
 */
public class A0001MigrationTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    // Minimal flat config for migration testing
    public static class FlatConfig extends OkaeriConfig {
        public boolean background_tasks = true;
        public String render_mode = "default";
    }

    // Nested config to test deep key migration
    public static class NestedConfig extends OkaeriConfig {
        public Inner inner = new Inner();

        public static class Inner extends OkaeriConfig {
            public String setting_name = "value";
            public boolean auto_reload = false;
        }
    }

    // Three-level nesting
    public static class ThreeLevelConfig extends OkaeriConfig {
        public Level1 level_one = new Level1();

        public static class Level1 extends OkaeriConfig {
            public Level2 level_two = new Level2();

            public static class Level2 extends OkaeriConfig {
                public String deep_key = "content";
            }
        }
    }

    @Test
    void flatKeyWithHyphenIsRenamedToUnderscore() throws IOException {
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", "background-tasks: false"), FlatConfig.class);

        assertFalse(content.contains("background-tasks:"), "Content: " + content);
        assertTrue(content.contains("background_tasks:"), "Content: " + content);
    }

    @Test
    void flatKeyWithoutHyphenIsUnchanged() throws IOException {
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", "background_tasks: true"), FlatConfig.class);

        assertTrue(content.contains("background_tasks:"), "Content: " + content);
        assertFalse(content.contains("background-tasks:"), "Content: " + content);
    }

    @Test
    void multipleHyphensInSingleKeyAllReplaced() throws IOException {
        // render-mode has a single hyphen
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", "render-mode: neon"), FlatConfig.class);

        assertFalse(content.contains("render-mode:"), "Content: " + content);
        assertTrue(content.contains("render_mode:"), "Content: " + content);
    }

    @Test
    void multipleSiblingKeysAllRenamed() throws IOException {
        final String yaml = "background-tasks: false\nrender-mode: neon";
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml), FlatConfig.class);

        assertFalse(content.contains("background-tasks:"), "Content: " + content);
        assertFalse(content.contains("render-mode:"), "Content: " + content);
        assertTrue(content.contains("background_tasks:"), "Content: " + content);
        assertTrue(content.contains("render_mode:"), "Content: " + content);
    }

    @Test
    void keyValuesWithHyphensAreNotReplaced() throws IOException {
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", "render_mode: \"hello-world\""), FlatConfig.class);

        assertTrue(content.contains("hello-world"), "Hyphenated VALUE should be preserved: " + content);
    }

    @Test
    void deeplyNestedKeyWithHyphenIsRenamed() throws IOException {
        final String yaml = "inner:\n  setting-name: foo\n  auto-reload: false";
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml), NestedConfig.class);

        assertFalse(content.contains("setting-name:"), "Content: " + content);
        assertFalse(content.contains("auto-reload:"), "Content: " + content);
        assertTrue(content.contains("setting_name:"), "Content: " + content);
        assertTrue(content.contains("auto_reload:"), "Content: " + content);
    }

    @Test
    void deeplyNestedNoHyphensUnchanged() throws IOException {
        final String yaml = "inner:\n  setting_name: bar\n  auto_reload: true";
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml), NestedConfig.class);

        assertTrue(content.contains("setting_name:"), "Content: " + content);
        assertTrue(content.contains("auto_reload:"), "Content: " + content);
        assertFalse(content.contains("setting-name:"), "Content: " + content);
        assertFalse(content.contains("auto-reload:"), "Content: " + content);
    }

    @Test
    void threeLayerNestingIsFullyRenamed() throws IOException {
        final String yaml = "level-one:\n  level-two:\n    deep-key: content";
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml), ThreeLevelConfig.class);

        assertFalse(content.contains("level-one:"), "Content: " + content);
        assertFalse(content.contains("level-two:"), "Content: " + content);
        assertFalse(content.contains("deep-key:"), "Content: " + content);
        assertTrue(content.contains("level_one:"), "Content: " + content);
        assertTrue(content.contains("level_two:"), "Content: " + content);
        assertTrue(content.contains("deep_key:"), "Content: " + content);
    }

    @Test
    void idempotentAfterFirstMigration() throws IOException {
        final Path file = ConfigTestSupport.writeYaml(tempDir, "config.yml", "background-tasks: false");
        final String afterFirst = buildAndReadFile(file, FlatConfig.class);
        final String afterSecond = buildAndReadFile(file, FlatConfig.class);

        assertEquals(afterFirst, afterSecond);
    }

    @Test
    void emptyConfigHandledGracefully() {
        assertDoesNotThrow(() -> buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", ""), FlatConfig.class));
    }

    @Test
    void mixedSiblingKeysOnlyHyphenatedOnesChanged() throws IOException {
        final String yaml = "background-tasks: false\nrender_mode: default";
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml), FlatConfig.class);

        assertFalse(content.contains("background-tasks:"), "Content: " + content);
        assertTrue(content.contains("background_tasks:"), "Content: " + content);
        assertTrue(content.contains("render_mode:"), "Content: " + content);
    }

    @Test
    void noHyphenatedKeysBothRemainSnakeCase() throws IOException {
        final String yaml = "background_tasks: true\nrender_mode: \"dark\"";
        final String content = buildAndReadFile(ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml), FlatConfig.class);

        assertTrue(content.contains("background_tasks:"), "Content: " + content);
        assertTrue(content.contains("render_mode:"), "Content: " + content);
    }
}
