package xyz.srnyx.annoyingapi.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.stats.loader.BStatsLoader;
import xyz.srnyx.annoyingapi.stats.loader.FastStatsLoader;
import xyz.srnyx.annoyingapi.stats.provider.BStatsProvider;
import xyz.srnyx.annoyingapi.stats.provider.FastStatsProvider;
import xyz.srnyx.javautilities.parents.Stringable;

import java.util.function.Consumer;


/**
 * Represents the options for statistics collection
 */
public class StatsOptions extends Stringable {
    @NotNull public BStatsOptions bStats = new BStatsOptions();
    @NotNull public FastStatsOptions fastStats = new FastStatsOptions();

    /**
     * Constructs a new {@link StatsOptions} instance with default values
     */
    public StatsOptions() {
        // Only exists to give the constructor a Javadoc
    }

    @NotNull
    public StatsOptions bStats(@NotNull BStatsOptions bStats) {
        this.bStats = bStats;
        return this;
    }

    @NotNull
    public StatsOptions bStats(@NotNull Consumer<BStatsOptions> consumer) {
        consumer.accept(bStats);
        return this;
    }

    @NotNull
    public StatsOptions fastStats(@NotNull FastStatsOptions fastStats) {
        this.fastStats = fastStats;
        return this;
    }

    @NotNull
    public StatsOptions fastStats(@NotNull Consumer<FastStatsOptions> consumer) {
        consumer.accept(fastStats);
        return this;
    }

    public static class BStatsOptions {
        /**
         * Convenience so that a separate {@link BStatsProvider} class doesn't need to be made (the API will create an anonymous one for you).
         * <br><i>This is overriden if a {@link BStatsProvider} class IS found during {@link RegistrationOptions.AutomaticRegistration automatic registration}.</i>
         */
        @Nullable public Class<? extends BStatsLoader> loader;
        /**
         * Even more of a convenience than {@link #loader} such that you provide <b>just</b> the ID, rather than an entire loader.
         * <br>However, this means you must define custom charts outside of a loader, which is not recommended.
         * <br>So, it's recommended to only use this if you don't want to define anything custom!
         * <br><i>This is overriden by {@link #loader} if both are provided.</i>
         */
        @Nullable public Integer id;
        /**
         * Whether to delete the old bstats.yml file when the plugin is loaded
         */
        public boolean deleteOldBStatsFile = true;

        /**
         * {@link #loader}
         */
        @NotNull
        public StatsOptions.BStatsOptions loader(@Nullable Class<? extends BStatsLoader> loader) {
            this.loader = loader;
            return this;
        }

        /**
         * {@link #id}
         */
        @NotNull
        public StatsOptions.BStatsOptions id(int id) {
            this.id = id;
            return this;
        }

        /**
         * {@link #deleteOldBStatsFile}
         */
        @NotNull
        public StatsOptions.BStatsOptions deleteOldBStatsFile(boolean deleteOldBStatsFile) {
            this.deleteOldBStatsFile = deleteOldBStatsFile;
            return this;
        }
    }

    public static class FastStatsOptions {
        /**
         * Convenience so that a separate {@link FastStatsProvider} class doesn't need to be made (the API will create an anonymous one for you).
         * <br><i>This is overriden if a {@link FastStatsProvider} class IS found during {@link RegistrationOptions.AutomaticRegistration automatic registration}.</i>
         */
        @Nullable public Class<? extends FastStatsLoader> loader;
        /**
         * Even more of a convenience than {@link #loader} such that you provide <b>just</b> the ID, rather than an entire loader.
         * <br>However, this means you must define custom metrics outside of a loader, which is not recommended.
         * <br>So, it's recommended to only use this if you don't want to define anything custom!
         * <br><i>This is overriden by {@link #loader} if both are provided.</i>
         */
        @Nullable public String id;

        /**
         * {@link #loader}
         */
        @NotNull
        public StatsOptions.FastStatsOptions loader(@Nullable Class<? extends FastStatsLoader> loader) {
            this.loader = loader;
            return this;
        }

        /**
         * {@link #id}
         */
        @NotNull
        public StatsOptions.FastStatsOptions id(@Nullable String id) {
            this.id = id;
            return this;
        }
    }
}
