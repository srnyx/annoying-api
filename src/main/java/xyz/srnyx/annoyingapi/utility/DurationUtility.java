package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;


public class DurationUtility {
    @Contract(pure = true)
    public static long toTicks(@NotNull Duration duration) {
        return duration.toMillis() / 50;
    }

    @Contract(pure = true)
    public static Duration fromTicks(long ticks) {
        return Duration.ofMillis(ticks * 50);
    }
}
