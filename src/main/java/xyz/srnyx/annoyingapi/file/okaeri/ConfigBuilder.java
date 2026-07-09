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
import xyz.srnyx.annoyingapi.AnnoyingPlugin;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.file.okaeri.migration.A0001_Rename_kebab_case_to_snake_case;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.*;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.color.ColorAttachmentResolver;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.color.ColorSerializer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.duration.DurationSerializer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeAttachmentResolver;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipe.RecipeSerializer;
import xyz.srnyx.annoyingapi.file.okaeri.serdes.recipechoice.RecipeChoiceSerializer;
import xyz.srnyx.annoyingapi.file.okaeri.validator.AnnoyingConfigValidator;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;


public class ConfigBuilder {
    @NotNull public final AnnoyingPlugin plugin;

    @NotNull private File file;
    @Nullable private OkaeriConfig config;
    @Nullable private Consumer<OkaeriConfigOptions> configure;
    @Nullable private Consumer<YamlBukkitConfigurer> configurer;
    @NotNull private final List<ConfigMigration> internalStateMigrations = new ArrayList<>();
    @NotNull private final List<ConfigMigration> configMigrations = new ArrayList<>();
    private boolean renameKebabCaseToSnakeCase = true;
    /**
     * Whether to delete the old file in the {@code default} folder from legacy {@link AnnoyingResource}
     */
    private boolean deleteOldDefaultFile = true;

    public ConfigBuilder(@NotNull AnnoyingPlugin plugin, @NotNull File file) {
        this.plugin = plugin;
        this.file = file;
    }

    /**
     * @param   name    relative to {@link JavaPlugin#getDataFolder()}
     */
    public ConfigBuilder(@NotNull AnnoyingPlugin plugin, @NotNull String name) {
        this(plugin, new File(plugin.getDataFolder(), name));
    }

    /**
     * {@code config.yml} in {@link JavaPlugin#getDataFolder()}
     */
    public ConfigBuilder(@NotNull AnnoyingPlugin plugin) {
        this(plugin, "config.yml");
    }

    @NotNull
    public ConfigBuilder file(@NotNull File file) {
        this.file = file;
        return this;
    }

    @NotNull
    public ConfigBuilder file(@NotNull String name) {
        return file(new File(plugin.getDataFolder(), name));
    }

    /**
     * Must be an instance of {@link OkaeriConfig}
     */
    @NotNull
    public ConfigBuilder config(@NotNull Object config) {
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
    public ConfigBuilder config(@NotNull Class<?> configClass) {
        if (!OkaeriConfig.class.isAssignableFrom(configClass)) {
            throw new IllegalArgumentException("Config class must extend OkaeriConfig: " + configClass.getName());
        }
        this.config = ConfigManager.create(configClass.asSubclass(OkaeriConfig.class));
        return this;
    }

    @NotNull
    public ConfigBuilder configure(@Nullable Consumer<OkaeriConfigOptions> configure) {
        this.configure = configure;
        return this;
    }

    @NotNull
    public ConfigBuilder configurer(@Nullable Consumer<YamlBukkitConfigurer> configurer) {
        this.configurer = configurer;
        return this;
    }

    @NotNull
    public ConfigBuilder internalStateMigrations(@NotNull Collection<ConfigMigration> migrations) {
        this.internalStateMigrations.addAll(migrations);
        return this;
    }

    @NotNull
    public ConfigBuilder internalStateMigrations(@NotNull ConfigMigration @NotNull ... migrations) {
        return internalStateMigrations(Arrays.asList(migrations));
    }

    @NotNull
    public ConfigBuilder configMigrations(@NotNull Collection<ConfigMigration> migrations) {
        this.configMigrations.addAll(migrations);
        return this;
    }

    @NotNull
    public ConfigBuilder configMigrations(@NotNull ConfigMigration @NotNull ... migrations) {
        return configMigrations(Arrays.asList(migrations));
    }

    @NotNull
    public ConfigBuilder renameKebabCaseToSnakeCase(boolean renameKebabCaseToSnakeCase) {
        this.renameKebabCaseToSnakeCase = renameKebabCaseToSnakeCase;
        return this;
    }

    @NotNull
    public ConfigBuilder deleteOldDefaultFile(boolean deleteOldDefaultFile) {
        this.deleteOldDefaultFile = deleteOldDefaultFile;
        return this;
    }

    @NotNull
    public <C> C build() {
        if (config == null) throw new IllegalStateException("Config must be set");

        // Configure
        config.configure(opt -> {
            // Configurer
            final YamlBukkitConfigurer configurer = new YamlBukkitConfigurer()
                    .setLineWidth(Integer.MAX_VALUE);
            if (this.configurer != null) this.configurer.accept(configurer);
            opt.configurer(
                    configurer,

                    // Okaeri serdes
                    new SerdesCommons(),
                    new SerdesBukkit(),

                    // Custom serdes
                    registry -> {
                        registry.register(new ColorSerializer());
                        registry.register(new DurationSerializer());
                        registry.register(new ColorAttachmentResolver());
                        registry.register(new RecipeSerializer(plugin));
                        registry.register(new RecipeAttachmentResolver());
                        registry.register(new RecipeChoiceSerializer());
                        registry.register(new AttributeModifierSerializer(plugin));
                        registry.register(new ItemStackSerializer());
                        registry.register(new JsonChatMessageSerializer(plugin));
                        registry.register(new JsonTitleMessageSerializer(plugin));
                        registry.register(new PlayableSoundSerializer());
                        registry.register(new PotionEffectSerializer());
                        registry.register(new XBaseSerializer());
                    });

            // Conditional serdes
            final NamespacedKeySerializer namespacedKeySerializer = new NamespacedKeySerializer(plugin);
            if (namespacedKeySerializer.get()) opt.serdes(namespacedKeySerializer);

            // Other options
            opt.validator(new AnnoyingConfigValidator());
            opt.bindFile(file);
            opt.removeOrphans(true);

            if (configure != null) configure.accept(opt);
        });

        // Save defaults (basically just creates file if it doesn't exist)
        config.saveDefaults();

        // Initial load (for migrations)
        config.load();

        // InternalStateMigrations
        final List<ConfigMigration> allInternalStateMigrations = new ArrayList<>();
        if (renameKebabCaseToSnakeCase) allInternalStateMigrations.add(new A0001_Rename_kebab_case_to_snake_case());
        allInternalStateMigrations.addAll(internalStateMigrations);
        config.migrateInternalState(allInternalStateMigrations.toArray(new ConfigMigration[0]));

        // ConfigMigrations
        config.migrate(configMigrations.toArray(new ConfigMigration[0]));

        // Manually save in-case no migrations occured
        config.save();

        // Delete old default file if new file in plugin data folder
        if (deleteOldDefaultFile) {
            final Path dataFolder = plugin.getDataFolder().toPath();
            final Path filePath = file.toPath();
            if (filePath.startsWith(dataFolder)) plugin.deleteOldFile(Path.of("default").resolve(dataFolder.relativize(filePath)));
        }

        // RootConfig#onLoad
        if (config instanceof final RootConfig rootConfig) rootConfig.onLoad();

        return (C) config;
    }
}
