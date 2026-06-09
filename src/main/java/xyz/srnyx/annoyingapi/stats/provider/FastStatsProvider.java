package xyz.srnyx.annoyingapi.stats.provider;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;
import xyz.srnyx.annoyingapi.library.RuntimeLibrary;
import xyz.srnyx.annoyingapi.stats.loader.FastStatsLoader;

import java.util.Collection;
import java.util.Collections;


public abstract class FastStatsProvider<L extends FastStatsLoader> extends StatsProvider<L> {
    @NotNull private final AnnoyingPlugin plugin;

    public FastStatsProvider(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override @NotNull
    public AnnoyingPlugin getAnnoyingPlugin() {
        return plugin;
    }

    @Override @NotNull
    public Collection<AnnoyingLibrary> getRequiredLibraries() {
        return Collections.singleton(RuntimeLibrary.FASTSTATS_BUKKIT);
    }
}
