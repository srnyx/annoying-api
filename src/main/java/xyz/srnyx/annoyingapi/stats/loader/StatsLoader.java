package xyz.srnyx.annoyingapi.stats.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.parents.Annoyable;


public abstract class StatsLoader<I, S> implements Annoyable {
    @Nullable public S stats;

    @NotNull
    public abstract I getId();

    public abstract void load();

    public void unload() {}
}
