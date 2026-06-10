package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;


public class SubConfig extends OkaeriConfig {
    @Override @NotNull
    public SubConfig save() {
        getContext().getRootConfig().save();
        return this;
    }
}
