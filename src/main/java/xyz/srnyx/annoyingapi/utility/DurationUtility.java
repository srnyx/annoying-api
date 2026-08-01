package xyz.srnyx.annoyingapi.utility;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
}
