package xyz.srnyx.annoyingapi.stats.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.stats.provider.StatsProvider;


public abstract class StatsLoader<P extends StatsProvider<?, P, ?>, S> {
    @Nullable public S stats;

    public abstract void load(@NotNull AnnoyingPlugin plugin, @NotNull P provider);

    public void unload() {}
}
