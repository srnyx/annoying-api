package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.migrate.builtin.NamedMigration;

import java.time.Duration;

import static eu.okaeri.configs.migrate.ConfigMigrationDsl.*;


public class S0001_Cache_interval_ticks_to_duration extends NamedMigration {
    public S0001_Cache_interval_ticks_to_duration() {
        super("migrates cache.interval from Minecraft ticks to a Duration",
                when(
                        // Check if current interval is a pure number (no units).
                        // This has *potential* for false positives if someone doesn't specify units.
                        ((config, view) -> {
                            final String interval = view.get("cache.interval", String.class);
                            if (interval != null) try {
                                Long.valueOf(interval);
                                return true;
                            } catch (final NumberFormatException ignored) {}
                            return false;
                        }),
                        update("cache.interval", old -> {
                            final long ticks = Long.parseLong(old.toString());
                            return Duration.ofMillis(ticks * 50L).toString(); // 1 tick = 50 ms
                        })));
    }
}
