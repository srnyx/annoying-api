package xyz.srnyx.annoyingapi.file.okaeri;

import be.seeseemelk.mockbukkit.MockBukkit;
import eu.okaeri.configs.OkaeriConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


/**
 * Base class for serializer tests that need real Bukkit objects (ItemStack, PotionEffect, etc.)
 * Uses MockBukkit to provide a real server implementation.
 */
public abstract class MockBukkitTestSupport {
    @BeforeEach
    void setUpMockBukkit() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDownMockBukkit() {
        MockBukkit.unmock();
    }

    /**
     * Writes {@code yaml} to {@code tempDir/test.yml} and loads it via {@link ConfigBuilder}.
     * Plugin-dependent serializers (RecipeSerializer, Json*, NamespacedKey) are NOT registered.
     */
    protected static <C extends OkaeriConfig> C loadFromYaml(Path tempDir, String yaml, Class<C> configClass) throws IOException {
        final Path path = ConfigTestSupport.writeYaml(tempDir, "test.yml", yaml);
        return new ConfigBuilder(new File(path.toString()))
                .config(configClass)
                .build();
    }
}
