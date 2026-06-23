package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.migrate.ConfigMigration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;


public class ConfigBuilderTest extends MockBukkitTestSupport {
    @TempDir(cleanup = CleanupMode.NEVER)
    Path tempDir;

    @Test
    void buildsExampleConfigFromClassAndLoadsExistingFile() throws IOException {
        final Path pluginData = tempDir.resolve("builder-plugin");
        ConfigTestSupport.writeYaml(pluginData, "config.yml", "");

        final ExampleConfig config = new ConfigBuilder(pluginData.resolve("config.yml").toFile())
                .config(ExampleConfig.class)
                .build();

        assertAll(
                () -> assertEquals("example-identity", config.identity.name),
                () -> assertEquals(ExampleConfig.Theme.DUSK, config.presentation.theme),
                () -> assertTrue(Files.exists(pluginData.resolve("config.yml"))));
    }

    @Test
    void configInstanceOverloadReturnsTheSameInstance() {
        final Path pluginData = tempDir.resolve("instance-plugin");
        final Path configFile = pluginData.resolve("config.yml");
        final ExampleConfig config = new ExampleConfig();
        config.identity.setMemo("set-before-build");

        final ExampleConfig loaded = new ConfigBuilder(configFile.toFile())
                .config(config)
                .build();

        assertSame(config, loaded);
        assertEquals("set-before-build", loaded.identity.getMemo());
        assertTrue(Files.exists(configFile));
    }

    @Test
    void configureCallbackCanOverrideDefaultOrphanRemovalAndCustomMigrationsRun() throws IOException {
        final Path configFile = ConfigTestSupport.writeYaml(tempDir, "custom.yml", """
                identity:
                  name: configured
                  build: 3
                orphan-key: keep-me
                """);
        final AtomicBoolean migrationRan = new AtomicBoolean(false);
        final ConfigMigration migration = (config, view) -> {
            migrationRan.set(true);
            view.setRaw("identity.build", 99);
            return true;
        };

        final ExampleConfig loaded = new ConfigBuilder(configFile.toFile())
                .config(ExampleConfig.class)
                .configure(configure -> configure.removeOrphans(false))
                .configMigrations(migration)
                .build();

        assertAll(
                () -> assertTrue(migrationRan.get()),
                () -> assertEquals("configured", loaded.identity.name),
                () -> assertEquals(99, loaded.identity.build),
                () -> assertEquals("keep-me", loaded.get("orphan_key")),
                () -> assertTrue(Files.exists(configFile)));
    }
}
