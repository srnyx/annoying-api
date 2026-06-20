package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;


public class SubConfig<R extends OkaeriConfig> extends OkaeriConfig {
    @NotNull
    public R getRootConfig() {
        return (R) getContext().getRootConfig();
    }

    @Override @NotNull
    public SubConfig<R> save() {
        getContext().getRootConfig().save();
        return this;
    }
}
