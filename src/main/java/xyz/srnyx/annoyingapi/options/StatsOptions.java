package xyz.srnyx.annoyingapi.options;

import org.jetbrains.annotations.NotNull;

import xyz.srnyx.annoyingapi.stats.provider.BStatsProvider;
import xyz.srnyx.annoyingapi.stats.provider.FastStatsProvider;
import xyz.srnyx.annoyingapi.stats.provider.StatsProvider;

import xyz.srnyx.javautilities.parents.Stringable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Represents the options for statistics collection
 *
 * @see BStatsProvider
 * @see FastStatsProvider
 */
public class StatsOptions extends Stringable {
    @NotNull public Set<StatsProvider<?, ?, ?>> providers = new HashSet<>();

    /**
     * Constructs a new {@link StatsOptions} instance with default values
     */
    public StatsOptions() {
        // Only exists to give the constructor a Javadoc
    }

    @NotNull
    public StatsOptions providers(@NotNull Collection<? extends StatsProvider<?, ?, ?>> providers) {
        this.providers.addAll(providers);
        return this;
    }

    @NotNull
    public StatsOptions providers(@NotNull StatsProvider<?, ?, ?>... providers) {
        return providers(Arrays.asList(providers));
    }
}
