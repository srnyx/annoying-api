package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.migrate.ConfigMigration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import xyz.srnyx.annoyingapi.MockBukkitTestSupport;
import xyz.srnyx.annoyingapi.file.okaeri.migration.A0001_Rename_kebab_case_to_snake_case;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class ConfigMigrationTest extends MockBukkitTestSupport {
    @TempDir(cleanup = CleanupMode.NEVER)
    Path tempDir;

    @Test
    void migratesKebabCaseMockConfigAndPreservesNestedValues() throws IOException {
        final Path configFile = writeConfig("""
                identity:
                  name: mock-identity
                  build: 11
                  active: false
                  memo: persisted-note
                  labels:
                    owner-name: Alice
                    render-mode: neon

                presentation:
                  theme: EMBER
                  density: DENSE
                  compact: true
                  panels:
                    - hero
                    - dashboard
                  palette:
                    primary: "#102030"
                    accent: "#c0ffee"
                    opacity: 42

                features:
                  mode: WILD
                  experimental: true
                  background-tasks: false
                  allowed-phases:
                    - INIT
                    - DRAIN
                  flag-overrides:
                    alpha: false
                    beta: true

                timing:
                  refresh-interval: 15
                  retry-window: 2
                  quick-timeout: 750
                  checkpoints:
                    - 4
                    - 8
                    - 12

                collections:
                  tokens:
                    - mock_one
                    - mock-two
                    - mock3
                  weights:
                    small: 2
                    medium: 5
                    large: 11
                  bags:
                    tags:
                      - red
                      - green
                    buckets:
                      LOW:
                        - dust
                      HIGH:
                        - spark
                        - flare

                rules:
                  thresholds:
                    minimum: 0.5
                    maximum: 0.9
                    warning-count: 7
                  gates:
                    channel: HYBRID
                    blocked-priorities:
                      - LOW
                    open: false
                  metadata:
                    owner: test-owner
                    history:
                      - draft
                      - review
                    note: private-note
                """);

        final ExampleConfig config = loadConfig(configFile, new A0001_Rename_kebab_case_to_snake_case());

        assertAll(
                () -> assertEquals("mock-identity", config.identity.name),
                () -> assertEquals(11, config.identity.build),
                () -> assertFalse(config.identity.active),
                () -> assertEquals("persisted-note", config.identity.getMemo()),
                () -> assertEquals(Map.of("owner_name", "Alice", "render_mode", "neon"), config.identity.labels),
                () -> assertEquals(ExampleConfig.Theme.EMBER, config.presentation.theme),
                () -> assertEquals(ExampleConfig.Density.DENSE, config.presentation.density),
                () -> assertTrue(config.presentation.compact),
                () -> assertEquals(List.of("hero", "dashboard"), config.presentation.panels),
                () -> assertEquals("#102030", config.presentation.palette.primary),
                () -> assertEquals("#c0ffee", config.presentation.palette.accent),
                () -> assertEquals(42, config.presentation.palette.opacity),
                () -> assertEquals(ExampleConfig.Mode.WILD, config.features.mode),
                () -> assertTrue(config.features.experimental),
                () -> assertFalse(config.features.background_tasks),
                () -> assertEquals(Set.of(ExampleConfig.Phase.INIT, ExampleConfig.Phase.DRAIN), config.features.allowed_phases),
                () -> assertEquals(Map.of("alpha", false, "beta", true), config.features.flag_overrides),
                () -> assertEquals(Duration.ofSeconds(15), config.timing.refresh_interval),
                () -> assertEquals(Duration.ofMinutes(2), config.timing.retry_window),
                () -> assertEquals(Duration.ofMillis(750), config.timing.quick_timeout),
                () -> assertEquals(List.of(4, 8, 12), config.timing.checkpoints),
                () -> assertEquals(Set.of("mock_one", "mock-two", "mock3"), config.collections.tokens),
                () -> assertEquals(Map.of("small", 2, "medium", 5, "large", 11), config.collections.weights),
                () -> assertEquals(List.of("red", "green"), config.collections.bags.tags),
                () -> assertEquals(List.of("dust"), config.collections.bags.buckets.get(ExampleConfig.Priority.LOW)),
                () -> assertEquals(List.of("spark", "flare"), config.collections.bags.buckets.get(ExampleConfig.Priority.HIGH)),
                () -> assertEquals(0.5d, config.rules.thresholds.minimum),
                () -> assertEquals(0.9d, config.rules.thresholds.maximum),
                () -> assertEquals(7, config.rules.thresholds.warning_count),
                () -> assertEquals(ExampleConfig.Channel.HYBRID, config.rules.gates.channel),
                () -> assertEquals(Set.of(ExampleConfig.Priority.LOW), config.rules.gates.blocked_priorities),
                () -> assertFalse(config.rules.gates.open),
                () -> assertEquals("test-owner", config.rules.metadata.owner),
                () -> assertEquals(List.of("draft", "review"), config.rules.metadata.history),
                () -> assertEquals("private-note", config.rules.metadata.getNote()));

        final String migrated = Files.readString(configFile, StandardCharsets.UTF_8);
        assertAll(
                () -> assertFalse(migrated.contains("background-tasks:")),
                () -> assertFalse(migrated.contains("allowed-phases:")),
                () -> assertFalse(migrated.contains("flag-overrides:")),
                () -> assertFalse(migrated.contains("refresh-interval:")),
                () -> assertFalse(migrated.contains("retry-window:")),
                () -> assertFalse(migrated.contains("quick-timeout:")),
                () -> assertFalse(migrated.contains("warning-count:")),
                () -> assertFalse(migrated.contains("blocked-priorities:")),
                () -> assertFalse(migrated.contains("owner-name:")),
                () -> assertTrue(migrated.contains("background_tasks:")),
                () -> assertTrue(migrated.contains("allowed_phases:")),
                () -> assertTrue(migrated.contains("flag_overrides:")),
                () -> assertTrue(migrated.contains("refresh_interval:")),
                () -> assertTrue(migrated.contains("retry_window:")),
                () -> assertTrue(migrated.contains("quick_timeout:")),
                () -> assertTrue(migrated.contains("warning_count:")),
                () -> assertTrue(migrated.contains("blocked_priorities:")),
                () -> assertTrue(migrated.contains("owner_name:")));
    }

    @Test
    void loadIsIdempotentAfterMigration() throws IOException {
        final Path configFile = writeConfig("""
                features:
                  background-tasks: false
                  allowed-phases:
                    - INIT

                timing:
                  refresh-interval: 15
                  retry-window: 2
                """);

        final ExampleConfig firstLoad = loadConfig(configFile, new A0001_Rename_kebab_case_to_snake_case());
        final String afterFirstLoad = Files.readString(configFile, StandardCharsets.UTF_8);

        final ExampleConfig secondLoad = loadConfig(configFile);
        final String afterSecondLoad = Files.readString(configFile, StandardCharsets.UTF_8);

        assertAll(
                () -> assertEquals(firstLoad.features.background_tasks, secondLoad.features.background_tasks),
                () -> assertEquals(firstLoad.features.allowed_phases, secondLoad.features.allowed_phases),
                () -> assertEquals(firstLoad.timing.refresh_interval, secondLoad.timing.refresh_interval),
                () -> assertEquals(firstLoad.timing.retry_window, secondLoad.timing.retry_window),
                () -> assertEquals(afterFirstLoad, afterSecondLoad));
    }

    @Test
    void migratesKebabCaseKeysInsideListOfMaps() throws IOException {
        final Path configFile = writeConfig("""
                collections:
                  entries:
                    - entry-name: alpha
                      nested-info:
                        inner-key: value1
                    - entry-name: beta
                """);

        final ExampleConfig config = loadConfig(configFile, new A0001_Rename_kebab_case_to_snake_case());

        final List<Map<String, Object>> entries = config.collections.entries;
        assertAll(
                () -> assertEquals(2, entries.size()),
                () -> assertEquals("alpha", entries.get(0).get("entry_name")),
                () -> assertEquals(Map.of("inner_key", "value1"), entries.get(0).get("nested_info")),
                () -> assertEquals("beta", entries.get(1).get("entry_name")));

        final String migrated = Files.readString(configFile, StandardCharsets.UTF_8);
        assertAll(
                () -> assertFalse(migrated.contains("entry-name:")),
                () -> assertFalse(migrated.contains("nested-info:")),
                () -> assertFalse(migrated.contains("inner-key:")),
                () -> assertTrue(migrated.contains("entry_name:")),
                () -> assertTrue(migrated.contains("nested_info:")),
                () -> assertTrue(migrated.contains("inner_key:")));
    }

    @Test
    void migratesKebabCaseKeysInHeterogeneousList() throws IOException {
        // "orphan-list" is intentionally not bound to any ExampleConfig field: it exercises the
        // migration's own internal-state walk (Map entries mixed with a non-Map scalar) without
        // the typed List<Map<String, Object>> binding rejecting a heterogeneous list on update().
        final Path configFile = writeConfig("""
                orphan-list:
                  - orphan-key: value
                  - just-a-string

                collections:
                  entries:
                    - entry-name: alpha
                """);

        final ExampleConfig config = assertDoesNotThrow(() -> loadConfig(configFile, new A0001_Rename_kebab_case_to_snake_case()));

        assertEquals("alpha", config.collections.entries.get(0).get("entry_name"));
    }

    @Test
    void doesNotDuplicateRenamedKeysInListOfMaps() throws IOException {
        final Path configFile = writeConfig("""
                collections:
                  entries:
                    - entry-name: alpha
                """);

        final ExampleConfig config = loadConfig(configFile, new A0001_Rename_kebab_case_to_snake_case());

        final Map<String, Object> entry = config.collections.entries.get(0);
        assertAll(
                () -> assertEquals(Set.of("entry_name"), entry.keySet()),
                () -> assertFalse(entry.containsKey("entry-name")),
                () -> assertEquals("alpha", entry.get("entry_name")));
    }

    @Test
    void loadIsIdempotentForListOfMapsAfterMigration() throws IOException {
        final Path configFile = writeConfig("""
                collections:
                  entries:
                    - entry-name: alpha
                      nested-info:
                        inner-key: value1
                """);

        final ExampleConfig firstLoad = loadConfig(configFile, new A0001_Rename_kebab_case_to_snake_case());
        final String afterFirstLoad = Files.readString(configFile, StandardCharsets.UTF_8);

        final ExampleConfig secondLoad = loadConfig(configFile);
        final String afterSecondLoad = Files.readString(configFile, StandardCharsets.UTF_8);

        assertAll(
                () -> assertEquals(firstLoad.collections.entries, secondLoad.collections.entries),
                () -> assertEquals(afterFirstLoad, afterSecondLoad));
    }

    @Test
    void migratesKebabCaseKeysInNestedListOfMapsInsideListItem() throws IOException {
        final Path configFile = writeConfig("""
                collections:
                  entries:
                    - entry-name: alpha
                      child-entries:
                        - grandchild-name: beta
                          deep-info:
                            deep-key: value1
                """);

        final ExampleConfig config = loadConfig(configFile, new A0001_Rename_kebab_case_to_snake_case());

        final Map<String, Object> entry = config.collections.entries.get(0);
        assertEquals("alpha", entry.get("entry_name"));

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> childEntries = (List<Map<String, Object>>) entry.get("child_entries");
        assertNotNull(childEntries);
        final Map<String, Object> grandchild = childEntries.get(0);
        assertEquals("beta", grandchild.get("grandchild_name"));

        @SuppressWarnings("unchecked")
        final Map<String, Object> deepInfo = (Map<String, Object>) grandchild.get("deep_info");
        assertEquals("value1", deepInfo.get("deep_key"));

        final String migrated = Files.readString(configFile, StandardCharsets.UTF_8);
        assertAll(
                () -> assertFalse(migrated.contains("child-entries:")),
                () -> assertFalse(migrated.contains("grandchild-name:")),
                () -> assertFalse(migrated.contains("deep-info:")),
                () -> assertFalse(migrated.contains("deep-key:")),
                () -> assertTrue(migrated.contains("child_entries:")),
                () -> assertTrue(migrated.contains("grandchild_name:")),
                () -> assertTrue(migrated.contains("deep_info:")),
                () -> assertTrue(migrated.contains("deep_key:")));
    }

    @NotNull
    private ExampleConfig loadConfig(@NotNull Path configFile, @NotNull ConfigMigration... migrations) {
        return configBuilder(configFile, ExampleConfig.class)
                .internalStateMigrations(migrations)
                .build();
    }

    @NotNull
    private Path writeConfig(@NotNull String yaml) throws IOException {
        return ConfigTestSupport.writeYaml(tempDir, "config.yml", yaml);
    }
}
