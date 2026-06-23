package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.duration.DurationSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigBuilder;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.MockBukkitTestSupport;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for the S0001 migration that converts {@code cache.interval} from Minecraft ticks (integer)
 * to a {@link Duration} string.
 *
 * <p>Each test writes a specific {@code cache.interval} value and checks the file content after migration.
 * In-memory Java objects reflect the loaded value before migration, so we read the file to confirm the migration ran.
 */
public class S0001MigrationTest extends MockBukkitTestSupport {
    @TempDir Path tempDir;

    public static class CacheConfig extends OkaeriConfig {
        public Cache cache = new Cache();

        public static class Cache extends OkaeriConfig {
            @DurationSpec(fallbackUnit = ChronoUnit.SECONDS)
            public Duration interval = Duration.ofSeconds(30);
        }
    }

    /** Loads the config with the S0001 migration registered, then returns the raw file content. */
    private String loadAndReadFile(String yaml) throws IOException {
        final Path file = ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml);
        new ConfigBuilder(new File(file.toString()))
                .config(CacheConfig.class)
                .internalStateMigrations(new S0001_Cache_interval_ticks_to_duration())
                .build();
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    /** Loads the file TWICE: first with migration, then without. Returns the config from the second load. */
    private CacheConfig loadWithMigrationThenReload(String yaml) throws IOException {
        final Path file = ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml);

        // First pass: apply migration
        new ConfigBuilder(new File(file.toString()))
                .config(CacheConfig.class)
                .internalStateMigrations(new S0001_Cache_interval_ticks_to_duration())
                .build();

        // Second pass: no migration; read migrated value
        return new ConfigBuilder(new File(file.toString()))
                .config(CacheConfig.class)
                .build();
    }

    @Test
    void numericTicks_20_yields1Second() throws IOException {
        final String content = loadAndReadFile("cache:\n  interval: 20");
        assertTrue(content.contains("1s"), "Expected 1s in: " + content);
    }

    @Test
    void numericTicks_20_javaObjectReflectsMigratedValue() throws IOException {
        final CacheConfig config = loadWithMigrationThenReload("cache:\n  interval: 20");
        assertEquals(Duration.ofSeconds(1), config.cache.interval);
    }

    @Test
    void numericTicks_0_yieldsPT0S() throws IOException {
        final String content = loadAndReadFile("cache:\n  interval: 0");
        assertTrue(content.contains("0"), "Expected 0 in: " + content);
    }

    @Test
    void numericTicks_15_yields750ms() throws IOException {
        final String content = loadAndReadFile("cache:\n  interval: 15");
        // 15 * 50ms = 750ms
        assertTrue(content.contains("750ms"), "Expected 750ms in: " + content);
    }

    @Test
    void numericTicks_400_yields20Seconds() throws IOException {
        final String content = loadAndReadFile("cache:\n  interval: 400");
        assertTrue(content.contains("20s"), "Expected 20s in: " + content);
    }

    @Test
    void numericTicks_72000_yields1Hour() throws IOException {
        final String content = loadAndReadFile("cache:\n  interval: 72000");
        assertTrue(content.contains("1h"), "Expected 1h in: " + content);
    }

    @Test
    void alreadyStringValue_isNotMigrated() throws IOException {
        // "20s" is not Long-parseable → guard returns false
        final String content = loadAndReadFile("cache:\n  interval: \"20s\"");
        assertFalse(content.contains("1s"), "Should NOT have been migrated: " + content);
    }

    @Test
    void existingDurationString_isNotMigrated() throws IOException {
        // "PT1S" is not Long-parseable → guard returns false
        final String content = loadAndReadFile("cache:\n  interval: 1s");
        assertTrue(content.contains("1s"), "Should still contain 1s: " + content);
        assertFalse(content.contains("50ms"), "Should NOT have been re-converted: " + content);
    }

    @Test
    void missingCacheSection_doesNotMigrate() throws IOException {
        // No cache key in YAML; default value is 30s
        final String content = loadAndReadFile("");
        assertTrue(content.contains("30s"), "Should NOT have been migrated: " + content);
    }

    @Test
    void decimalStringNotMigrated() throws IOException {
        // "15.5" is not a valid Long → guard returns false
        final String content = loadAndReadFile("cache:\n  interval: \"15.5\"");
        assertFalse(content.contains("s"), "Decimal should not trigger migration: " + content);
    }

    @Test
    void migrationIsIdempotent() throws IOException {
        final Path file = ConfigTestSupport.writeYaml(tempDir, "idempotent.yml", "cache:\n  interval: 20");

        // First pass: migrate 20 → PT1S
        new ConfigBuilder(new File(file.toString()))
                .config(CacheConfig.class)
                .configMigrations(new S0001_Cache_interval_ticks_to_duration())
                .build();
        final String afterFirst = Files.readString(file, StandardCharsets.UTF_8);

        // Second pass: migration should not trigger again (PT1S is not Long-parseable)
        new ConfigBuilder(new File(file.toString()))
                .config(CacheConfig.class)
                .configMigrations(new S0001_Cache_interval_ticks_to_duration())
                .build();
        final String afterSecond = Files.readString(file, StandardCharsets.UTF_8);

        assertEquals(afterFirst, afterSecond);
    }

    @Test
    void migrationReturns1TickProperly() throws IOException {
        // 1 tick = 50ms
        final String content = loadAndReadFile("cache:\n  interval: 1");
        assertTrue(content.contains("50ms"), "Expected 50ms in: " + content);
    }
}
