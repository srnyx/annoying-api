package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.srnyx.javautilities.MiscUtility;

import java.time.Duration;


public class DurationUtility {
    /**
     * Milliseconds to Minecraft ticks
     */
    @Contract(pure = true)
    public static long millisToTicks(long milliseconds) {
        return milliseconds / 50;
    }

    /**
     * {@link Duration} to Minecraft ticks
     */
    @Contract(pure = true)
    public static long durationToTicks(@NotNull Duration duration) {
        return millisToTicks(duration.toMillis());
    }

    /**
     * Minecraft ticks to milliseconds
     */
    @Contract(pure = true)
    public static long ticksToMillis(long ticks) {
        return ticks * 50;
    }

    /**
     * Minecraft ticks to {@link Duration}
     */
    @Contract(pure = true)
    public static Duration ticksToDuration(long ticks) {
        return Duration.ofMillis(ticksToMillis(ticks));
    }

    /**
     * @param   min inclusive
     * @param   max inclusive
     */
    @NotNull
    public static Duration getRandomDuration(@NotNull Duration min, @NotNull Duration max) {
        final long minMillis = min.toMillis();
        final long maxMillis = max.toMillis();
        if (minMillis >= maxMillis) return max;
        return Duration.ofMillis(MiscUtility.RANDOM.nextLong(minMillis, maxMillis == Long.MAX_VALUE ? Long.MAX_VALUE : maxMillis + 1));
    }
}
