package xyz.srnyx.annoyingapi.file.okaeri.migration;

import eu.okaeri.configs.exception.OkaeriConfigException;
import eu.okaeri.configs.migrate.builtin.NamedMigration;

import java.time.Duration;


public class S0001_Cache_interval_ticks_to_duration extends NamedMigration {
    public S0001_Cache_interval_ticks_to_duration() {
        super("migrates cache.interval from Minecraft ticks to a Duration",
                // Convert if current interval is a pure number (no units).
                // This has *potential* for false positives if someone doesn't specify units.
                ((config, view) -> {
                    // Get interval as Long
                    final Long interval;
                    try {
                        interval = view.get("cache.interval", Long.class);
                    } catch (final OkaeriConfigException e) {
                        return false;
                    }
                    if (interval == null) return false;

                    // Convert to Duration
                    try {
                        view.set("cache.interval", Duration.ofMillis(interval * 50L)); // 1 tick = 50 ms
                        return true;
                    } catch (final NumberFormatException ignored) {
                        return false;
                    }
                }));
    }
}
