package xyz.srnyx.annoyingapi.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.stats.loader.BStatsLoader;
import xyz.srnyx.annoyingapi.stats.loader.FastStatsLoader;
import xyz.srnyx.annoyingapi.stats.provider.BStatsProvider;
import xyz.srnyx.annoyingapi.stats.provider.FastStatsProvider;

import xyz.srnyx.javautilities.parents.Stringable;


/**
 * Represents the options for statistics collection
 */
public class StatsOptions extends Stringable {
    /**
     * Convenience so that a separate {@link BStatsProvider} class doesn't need to be made (the API will create an anonymous one for you).
     * <br>If a {@link BStatsProvider} class IS found during {@link RegistrationOptions.AutomaticRegistration automatic registration}, this will be ignored.
     */
    @Nullable public Class<? extends BStatsLoader> bStatsLoader;
    /**
     * Convenience so that a separate {@link FastStatsProvider} class doesn't need to be made (the API will create an anonymous one for you).
     * <br>If a {@link FastStatsProvider} class IS found during {@link RegistrationOptions.AutomaticRegistration automatic registration}, this will be ignored.
     */
    @Nullable public Class<? extends FastStatsLoader> fastStatsLoader;

    /**
     * Constructs a new {@link StatsOptions} instance with default values
     */
    public StatsOptions() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * {@link #bStatsLoader}
     */
    @NotNull
    public StatsOptions bStatsLoader(@Nullable Class<? extends BStatsLoader> bStatsLoader) {
        this.bStatsLoader = bStatsLoader;
        return this;
    }

    /**
     * {@link #fastStatsLoader}
     */
    @NotNull
    public StatsOptions fastStatsLoader(@Nullable Class<? extends FastStatsLoader> fastStatsLoader) {
        this.fastStatsLoader = fastStatsLoader;
        return this;
    }
}
