package xyz.srnyx.annoyingapi;

import be.seeseemelk.mockbukkit.MockBukkit;
import eu.okaeri.configs.OkaeriConfig;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Base class for serializer tests that need real Bukkit objects (ItemStack, PotionEffect, etc.)
 * Uses MockBukkit to provide a real server implementation.
 */
public abstract class MockBukkitTestSupport {
    protected static MockAnnoyingPlugin PLUGIN;

    @BeforeAll
    static void setUpMockBukkit() {
        MockBukkit.mock();
        PLUGIN = MockBukkit.load(MockAnnoyingPlugin.class);
    }

    @AfterAll
    static void tearDownMockBukkit() {
        MockBukkit.unmock();
    }

    @BeforeEach
    void cleanDataFolder() {
        try {
            FileUtils.cleanDirectory(PLUGIN.getDataFolder());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected ConfigBuilder configBuilder(@NotNull Path path, Class<? extends OkaeriConfig> configClass) {
        return new ConfigBuilder(PLUGIN, new File(path.toString())).config(configClass);
    }

    protected <C extends OkaeriConfig> C loadConfig(@NotNull Path tempDir, @NotNull String yaml, @NotNull Class<C> cls) throws IOException {
        final Path path = ConfigTestSupport.writeYaml(tempDir, cls.getName() + ".yml", yaml);
        return configBuilder(path, cls).build();
    }

    protected String buildAndReadFile(@NotNull Path file, @NotNull Class<? extends OkaeriConfig> cls) throws IOException {
        configBuilder(file, cls).build();
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
