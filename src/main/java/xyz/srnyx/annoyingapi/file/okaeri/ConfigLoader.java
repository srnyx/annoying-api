package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.library.RuntimeLibrary;

import java.util.function.Consumer;


public class ConfigLoader {
    @NotNull private final AnnoyingPlugin plugin;

    public ConfigLoader(@NotNull AnnoyingPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public <C extends OkaeriConfig> C buildElseNull(@NotNull Consumer<ConfigBuilder<C>> builder) {
        // Load libraries
        if (!plugin.libraryManager.loadIfNotLoaded(RuntimeLibrary.OKAERI_CONFIGS_YAML_BUKKIT)) return null;
        if (!plugin.libraryManager.loadIfNotLoaded(RuntimeLibrary.OKAERI_CONFIGS_SERDES_COMMONS)) return null;
        if (!plugin.libraryManager.loadIfNotLoaded(RuntimeLibrary.OKAERI_CONFIGS_SERDES_BUKKIT)) return null;
        if (!plugin.libraryManager.loadIfNotLoaded(RuntimeLibrary.OKAERI_CONFIGS_VALIDATOR_OKAERI)) return null;

        // Build config
        final ConfigBuilder<C> configBuilder = new ConfigBuilder<>(plugin);
        builder.accept(configBuilder);
        return configBuilder.build();
    }

    @NotNull
    public <C extends OkaeriConfig> C buildElseThrow(@NotNull Consumer<ConfigBuilder<C>> builder) {
        C config = buildElseNull(builder);
        if (config == null) throw new IllegalStateException("Failed to load config due to missing libraries");
        return config;
    }
}
