package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.serdes.commons.duration.DurationSpec;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.DurationRange;
import xyz.srnyx.annoyingapi.file.okaeri.validator.annotation.PatternCollection;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Header("# --- MOCK SAMPLE ---")
@Header("# This file exists only to exercise config shapes in tests.")
@Header("# Values, names, and nesting are intentionally fictional.")
public class ExampleConfig extends OkaeriConfig {
    @Comment("High-level identity information for the pretend system")
    @NotNull public Identity identity = new Identity();

    @Comment("Presentation-related values for the pretend system")
    @NotNull public Presentation presentation = new Presentation();

    @Comment("Feature toggles and mode switches")
    @NotNull public Features features = new Features();

    @Comment("Timing and retry-like values")
    @NotNull public Timing timing = new Timing();

    @Comment("Collection-heavy values for serializer coverage")
    @NotNull public Collections collections = new Collections();

    @Comment("Nested rules, thresholds, and gates")
    @NotNull public Rules rules = new Rules();

    public static class Identity extends OkaeriConfig {
        @Comment("A fake identifier")
        @NotNull public String name = "example-identity";

        @Comment("A fake build number")
        public int build = 7;

        @Comment("Whether the record is active")
        public boolean active = true;

        @Comment("A private note with accessors to show bean-style fields")
        @NotNull private String memo = "internal-mock-note";

        @Comment("Free-form labels for the identity block")
        @NotNull public Map<String, String> labels = new LinkedHashMap<>(Map.of(
                "owner", "nobody",
                "realm", "sandbox"));

        @NotNull
        public String getMemo() {
            return memo;
        }

        public void setMemo(@NotNull String memo) {
            this.memo = memo;
        }
    }

    public static class Presentation extends OkaeriConfig {
        @Comment("The visual theme to pretend to use")
        @NotNull public Theme theme = Theme.DUSK;

        @Comment("How densely information should be packed")
        public Density density = Density.MEDIUM;

        @Comment("Whether compact rendering is enabled")
        public boolean compact = false;

        @Comment("A few fake panel names")
        @NotNull public List<String> panels = new ArrayList<>(List.of("alpha", "beta", "gamma"));

        @Comment("A nested color palette example")
        @NotNull public Palette palette = new Palette();

        public static class Palette extends OkaeriConfig {
            @Comment("Primary color token")
            @NotNull public String primary = "#224466";

            @Comment("Accent color token")
            @NotNull public String accent = "#ee7744";

            @Comment("Opacity as a percent-like integer")
            public int opacity = 88;
        }
    }

    public static class Features extends OkaeriConfig {
        @Comment("The pretend operating mode")
        @NotNull public Mode mode = Mode.SAFE;

        @Comment("Whether the experimental branch is on")
        public boolean experimental = false;

        @Comment("Whether background tasks are enabled")
        public boolean background_tasks = true;

        @Comment("Allowed phases for the pretend system")
        @NotNull public Set<Phase> allowed_phases = new HashSet<>(Set.of(Phase.INIT, Phase.STEADY));

        @Comment("Named toggles for fake feature flags")
        @NotNull public Map<String, Boolean> flag_overrides = new LinkedHashMap<>(Map.of(
                "alpha", true,
                "beta", false));
    }

    public static class Timing extends OkaeriConfig {
        @Comment("How often the mock refresh happens")
        @DurationSpec(fallbackUnit = ChronoUnit.SECONDS)
        @DurationRange(min = 5, minUnit = ChronoUnit.SECONDS)
        @NotNull public Duration refresh_interval = Duration.ofSeconds(15);

        @Comment("How long the system should wait before retrying")
        @DurationSpec(fallbackUnit = ChronoUnit.MINUTES)
        @DurationRange(min = 1, minUnit = ChronoUnit.MINUTES)
        @NotNull public Duration retry_window = Duration.ofMinutes(2);

        @Comment("Sample millisecond-scale timeout")
        @DurationSpec(fallbackUnit = ChronoUnit.MILLIS)
        @NotNull public Duration quick_timeout = Duration.ofMillis(750);

        @Comment("Ordered checkpoints for faux scheduling")
        @NotNull public List<Integer> checkpoints = new ArrayList<>(List.of(3, 9, 27));
    }

    public static class Collections extends OkaeriConfig {
        @Comment("A set of arbitrary tokens that match a pattern")
        @PatternCollection("^[a-z][a-z0-9_-]{2,15}$")
        @NotNull public Set<String> tokens = new HashSet<>(Set.of("mock_one", "mock-two", "mock3"));

        @Comment("A tiny catalog of numeric weights")
        @NotNull public Map<String, Integer> weights = new LinkedHashMap<>(Map.of(
                "small", 2,
                "medium", 5,
                "large", 11));

        @Comment("A nested bag of sub-collections")
        @NotNull public Bags bags = new Bags();

        public static class Bags extends OkaeriConfig {
            @Comment("A sequence of placeholder tags")
            @NotNull public List<String> tags = new ArrayList<>(List.of("red", "green", "blue"));

            @Comment("Different buckets by priority")
            @NotNull public Map<Priority, List<String>> buckets = new EnumMap<>(Priority.class);

            public Bags() {
                buckets.put(Priority.LOW, new ArrayList<>(List.of("dust")));
                buckets.put(Priority.HIGH, new ArrayList<>(List.of("spark", "flare")));
            }
        }
    }

    public static class Rules extends OkaeriConfig {
        @Comment("Overall threshold settings")
        @NotNull public Thresholds thresholds = new Thresholds();

        @Comment("Gating and routing examples")
        @NotNull public Gates gates = new Gates();

        @Comment("Additional nested metadata")
        @NotNull public Metadata metadata = new Metadata();

        public static class Thresholds extends OkaeriConfig {
            @Comment("Minimum accepted value")
            public double minimum = 0.25d;

            @Comment("Maximum accepted value")
            public double maximum = 0.95d;

            @Comment("Soft limit used for warnings")
            public int warning_count = 12;
        }

        public static class Gates extends OkaeriConfig {
            @Comment("The channel used by the pretend gate")
            @NotNull public Channel channel = Channel.HYBRID;

            @Comment("Which priorities should be blocked")
            @NotNull public Set<Priority> blocked_priorities = new HashSet<>(Set.of(Priority.LOW));

            @Comment("Whether the gate is open")
            public boolean open = true;
        }

        public static class Metadata extends OkaeriConfig {
            @Comment("A fake owner field")
            @NotNull public String owner = "anonymous";

            @Comment("Revision history entries")
            @NotNull public List<String> history = new ArrayList<>(List.of("draft", "review", "final"));

            @Comment("Miscellaneous notes")
            @NotNull private String note = "nested-private-note";

            @NotNull
            public String getNote() {
                return note;
            }

            public void setNote(@NotNull String note) {
                this.note = note;
            }
        }
    }

    public enum Theme {
        DUSK,
        PAPER,
        EMBER
    }

    public enum Density {
        SPARSE,
        MEDIUM,
        DENSE
    }

    public enum Mode {
        OFF,
        SAFE,
        WILD
    }

    public enum Phase {
        INIT,
        STEADY,
        DRAIN
    }

    public enum Priority {
        LOW,
        HIGH,
        CRITICAL
    }

    public enum Channel {
        LOCAL,
        REMOTE,
        HYBRID
    }
}
