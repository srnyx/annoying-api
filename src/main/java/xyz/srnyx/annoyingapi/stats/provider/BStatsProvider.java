package xyz.srnyx.annoyingapi.stats.provider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;
import xyz.srnyx.annoyingapi.library.RuntimeLibrary;
import xyz.srnyx.annoyingapi.stats.loader.BStatsLoader;

import java.util.Arrays;
import java.util.List;


public class BStatsProvider extends StatsProvider<Integer, BStatsProvider, BStatsLoader> {
    public BStatsProvider(int id) {
        super(id);
    }

    @Override @NotNull
    public Class<BStatsLoader> getLoaderClass() {
        return BStatsLoader.class;
    }

    @Override @Nullable
    public List<AnnoyingLibrary> getRequiredLibraries() {
        return Arrays.asList(RuntimeLibrary.BSTATS_BASE, RuntimeLibrary.BSTATS_BUKKIT);
    }
}
