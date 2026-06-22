package xyz.srnyx.annoyingapi.file.okaeri;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;

import java.util.function.Consumer;
import java.util.logging.Level;


public class ConfigLoader {
    @NotNull private final AnnoyingPlugin plugin;

    public ConfigLoader(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public <C> C build(@NotNull Consumer<ConfigBuilder> builder) {
        final ConfigBuilder configBuilder = new ConfigBuilder(plugin);
        builder.accept(configBuilder);
        final C config = configBuilder.build();
        AnnoyingPlugin.log(Level.INFO, "Loaded config: " + config.getClass().getName());
        return config;
    }
}
