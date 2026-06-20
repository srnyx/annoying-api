package xyz.srnyx.annoyingapi.file.okaeri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.library.AnnoyingAPILibrary;

import java.util.function.Consumer;
import java.util.logging.Level;


public class ConfigLoader {
    @NotNull private final AnnoyingPlugin plugin;

    public ConfigLoader(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public <C> C buildElseNull(@NotNull Consumer<ConfigBuilder> builder) {
        if (!plugin.libraryManager.loadIfNotLoaded(
                AnnoyingAPILibrary.OKAERI_CONFIGS_YAML_BUKKIT,
                AnnoyingAPILibrary.OKAERI_CONFIGS_SERDES_COMMONS,
                AnnoyingAPILibrary.OKAERI_CONFIGS_SERDES_BUKKIT,
                AnnoyingAPILibrary.OKAERI_CONFIGS_VALIDATOR_OKAERI)) return null;

        final ConfigBuilder configBuilder = new ConfigBuilder(plugin);
        builder.accept(configBuilder);
        final C config = configBuilder.build();
        AnnoyingPlugin.log(Level.INFO, "Loaded config: " + config.getClass().getName());
        return config;
    }

    @NotNull
    public <C> C buildElseThrow(@NotNull Consumer<ConfigBuilder> builder) {
        final C config = buildElseNull(builder);
        if (config == null) throw new IllegalStateException("Failed to load a config due to missing libraries");
        return config;
    }
}
