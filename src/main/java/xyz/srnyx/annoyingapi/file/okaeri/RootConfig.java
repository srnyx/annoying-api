package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;


public class RootConfig extends OkaeriConfig {
    public void onLoad() {}

    public void reload() {
        this.load(true);
        this.onLoad();
    }
}
