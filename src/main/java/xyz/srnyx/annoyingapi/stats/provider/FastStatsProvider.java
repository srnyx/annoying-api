package xyz.srnyx.annoyingapi.stats.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;
import xyz.srnyx.annoyingapi.library.RuntimeLibrary;
import xyz.srnyx.annoyingapi.stats.loader.FastStatsLoader;

import java.util.Arrays;
import java.util.List;


public class FastStatsProvider extends StatsProvider<String, FastStatsProvider, FastStatsLoader> {
    public FastStatsProvider(@NotNull String id) {
        super(id);
    }

    @Override @NotNull
    public Class<FastStatsLoader> getLoaderClass() {
        return FastStatsLoader.class;
    }

    @Override @Nullable
    public List<AnnoyingLibrary> getRequiredLibraries() {
        return Arrays.asList(RuntimeLibrary.FASTSTATS_CORE, RuntimeLibrary.FASTSTATS_BUKKIT);
    }
}
