package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;


public class SubConfig<R extends OkaeriConfig> extends OkaeriConfig {
    @NotNull public final R root;

    public SubConfig(@NotNull R root) {
        this.root = root;
    }

    @Override @NotNull
    public SubConfig<R> save() {
        root.save();
        return this;
    }
}
