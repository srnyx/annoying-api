package xyz.srnyx.annoyingapi.file.okaeri;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.OkaeriConfigOptions;
import eu.okaeri.configs.migrate.ConfigMigration;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.file.okaeri.migration.A0001_Rename_kebab_case_to_snake_case;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.PlayableSoundSerializer;
import xyz.srnyx.annoyingapi.file.okaeri.validator.AnnoyingConfigValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;


public class ConfigBuilder<C> {
    @NotNull private final File file;
    @Nullable private OkaeriConfig config;
    @Nullable private Consumer<OkaeriConfigOptions> configure;
    @NotNull private final List<ConfigMigration> migrations = new ArrayList<>();
    private boolean renameKebabCaseToSnakeCase = true;
    private boolean saveDefaults = true;

    public ConfigBuilder(@NotNull File file) {
        this.file = file;
    }

    /**
     * @param   name    relative to {@link JavaPlugin#getDataFolder()}
     */
    public ConfigBuilder(@NotNull JavaPlugin plugin, @NotNull String name) {
        this(new File(plugin.getDataFolder(), name));
    }

    /**
     * {@code config.yml} in {@link JavaPlugin#getDataFolder()}
     */
    public ConfigBuilder(@NotNull JavaPlugin plugin) {
        this(plugin, "config.yml");
    }

    /**
     * Must be an instance of {@link OkaeriConfig}
     */
    @NotNull
    public ConfigBuilder<C> config(@NotNull C config) {
        if (!(config instanceof final OkaeriConfig okaeriConfig)) {
            throw new IllegalArgumentException("Config instance must extend OkaeriConfig: " + config.getClass().getName());
        }
        this.config = okaeriConfig;
        return this;
    }

    /**
     * Must be an instance of {@link OkaeriConfig}
     */
    @NotNull
    public ConfigBuilder<C> config(@NotNull Class<C> configClass) {
        if (!OkaeriConfig.class.isAssignableFrom(configClass)) {
            throw new IllegalArgumentException("Config class must extend OkaeriConfig: " + configClass.getName());
        }
        this.config = ConfigManager.create(configClass.asSubclass(OkaeriConfig.class));
        return this;
    }

    @NotNull
    public ConfigBuilder<C> configure(@Nullable Consumer<OkaeriConfigOptions> configure) {
        this.configure = configure;
        return this;
    }

    @NotNull
    public ConfigBuilder<C> migrations(@NotNull Collection<ConfigMigration> migrations) {
        this.migrations.addAll(migrations);
        return this;
    }

    @NotNull
    public ConfigBuilder<C> migration(@NotNull ConfigMigration @NotNull ... migration) {
        return migrations(Arrays.asList(migration));
    }

    @NotNull
    public ConfigBuilder<C> renameKebabCaseToSnakeCase(boolean renameKebabCaseToSnakeCase) {
        this.renameKebabCaseToSnakeCase = renameKebabCaseToSnakeCase;
        return this;
    }

    @NotNull
    public ConfigBuilder<C> saveDefaults(boolean saveDefaults) {
        this.saveDefaults = saveDefaults;
        return this;
    }

    @NotNull
    public C build() {
        if (config == null) throw new IllegalStateException("Config must be set");

        // Configure
        config.configure(opt -> {
            opt.configurer(
                    new YamlBukkitConfigurer(),
                    new SerdesCommons(),
                    new SerdesBukkit(),
                    registry -> {
                        registry.register(new PlayableSoundSerializer());
                    });
            opt.validator(new AnnoyingConfigValidator());
            opt.bindFile(file);
            opt.removeOrphans(true);

            if (configure != null) configure.accept(opt);
        });

        // Initial load (for migrations)
        config.load();

        // Migrations
        final List<ConfigMigration> migrations = new ArrayList<>();
        if (renameKebabCaseToSnakeCase) migrations.add(new A0001_Rename_kebab_case_to_snake_case());
        migrations.addAll(this.migrations);
        config.migrate(migrations.toArray(new ConfigMigration[0]));

        // Save defaults
        if (saveDefaults) config.saveDefaults();

        return (C) config;
    }
}
