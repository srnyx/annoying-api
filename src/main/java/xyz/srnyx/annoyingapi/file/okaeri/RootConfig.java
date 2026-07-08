package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;


public class RootConfig extends OkaeriConfig {
    public void onLoad() {}

    @Override @NotNull
    public OkaeriConfig load(@NotNull InputStream stream) {
        final OkaeriConfig config = super.load(stream);
        this.onLoad();
        return config;
    }
}
