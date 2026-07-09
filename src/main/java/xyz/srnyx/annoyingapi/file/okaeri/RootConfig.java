package xyz.srnyx.annoyingapi.file.okaeri;


public class RootConfig extends AnnoyingConfig {
    public void onLoad() {}

    public void reload() {
        this.load(true);
        this.onLoad();
    }
}
