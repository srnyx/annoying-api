package xyz.srnyx.annoyingapi;

import eu.okaeri.configs.OkaeriConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.byteflux.libby.relocation.Relocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.command.selector.SelectorManager;
import xyz.srnyx.annoyingapi.cooldown.CooldownManager;
import xyz.srnyx.annoyingapi.data.EntityData;
import xyz.srnyx.annoyingapi.file.okaeri.ConfigLoader;
import xyz.srnyx.annoyingapi.file.okaeri.migration.S0001_Cache_interval_ticks_to_duration;
import xyz.srnyx.annoyingapi.library.AnnoyingAPILibrary;
import xyz.srnyx.annoyingapi.library.AnnoyingLibrary;
import xyz.srnyx.annoyingapi.message.AnnoyingMessages;
import xyz.srnyx.annoyingapi.message.MessagesProvider;
import xyz.srnyx.annoyingapi.options.AnnoyingOptions;
import xyz.srnyx.annoyingapi.scheduler.AnnoyingScheduler;
import xyz.srnyx.annoyingapi.stats.StatsHelper;
import xyz.srnyx.annoyingapi.stats.loader.BStatsLoader;
import xyz.srnyx.annoyingapi.stats.loader.FastStatsLoader;
import xyz.srnyx.annoyingapi.stats.provider.BStatsProvider;
import xyz.srnyx.annoyingapi.stats.provider.FastStatsProvider;
import xyz.srnyx.annoyingapi.stats.provider.StatsProvider;
import xyz.srnyx.annoyingapi.storage.ConnectionException;
import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.storage.StorageConfig;
import xyz.srnyx.annoyingapi.storage.dialects.sql.SQLDialect;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.events.AdvancedPlayerMoveEvent;
import xyz.srnyx.annoyingapi.events.PlayerDamageByPlayerEvent;
import xyz.srnyx.annoyingapi.library.AnnoyingLibraryManager;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;
import xyz.srnyx.javautilities.MapGenerator;
import xyz.srnyx.javautilities.objects.SemanticVersion;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * Represents a plugin using Annoying API
 */
@SuppressWarnings("EmptyMethod")
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * Uses temporary initialization until the {@link AnnoyingPlugin plugin} is constructed (loaded)
     */
    @NotNull public static Logger LOGGER = new Logger("AnnoyingAPI - ?", null) {
        @Override
        public void log(@NotNull LogRecord logRecord) {
            logRecord.setMessage("[AnnoyingAPI - ?] " + logRecord.getMessage());
            super.log(logRecord);
        }
    };
    @NotNull public static final ServerSoftware SERVER_SOFTWARE = ServerSoftware.get();
    @NotNull public static final SemanticVersion MINECRAFT_VERSION = new SemanticVersion(Bukkit.getVersion().split("MC: ")[1].split("\\)")[0]);

    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = AnnoyingOptions.load(getResource("plugin.yml"));
    /**
     * The {@link AnnoyingLibraryManager} for the plugin to manage {@link AnnoyingLibrary libraries}
     */
    @NotNull public final AnnoyingLibraryManager libraryManager = new AnnoyingLibraryManager(this, "libs");
    /**
     * Loader for OkaeriConfig configs
     */
    @NotNull public final ConfigLoader configLoader;
    /**
     * Helper for stats providers (bStats, FastStats, etc.)
     */
    @NotNull public final StatsHelper statsHelper = new StatsHelper(this);
    /**
     * The {@link DataManager} for the plugin
     */
    @Nullable public DataManager dataManager;
    /**
     * Set of registered {@link Registrable}s by the plugin
     */
    @NotNull public final Set<Registrable> registeredClasses = new HashSet<>();
    /**
     * Custom events/listeners for the API
     */
    @NotNull public final Map<Class<? extends Event>, AnnoyingListener> customEvents = MapGenerator.HASH_MAP.mapOf(
            AdvancedPlayerMoveEvent.class, new AdvancedPlayerMoveEvent.Handler(this),
            PlayerDamageByPlayerEvent.class, new PlayerDamageByPlayerEvent.Handler(this));
    /**
     * The {@link SelectorManager} for the plugin
     */
    @NotNull public final SelectorManager selectorManager = new SelectorManager(this);
    /**
     * The {@link CooldownManager} for the plugin
     */
    @NotNull public final CooldownManager cooldownManager = new CooldownManager();
    /**
     * The {@link AnnoyingScheduler} for the plugin, used to run scheduled tasks in place of {@link BukkitScheduler}
     */
    @NotNull public final AnnoyingScheduler scheduler = new AnnoyingScheduler(this);
    @Nullable public UpdateChecker updateChecker;
    /**
     * Whether PlaceholderAPI is installed
     */
    public boolean papiInstalled = false;

    /**
     * Constructs a new {@link AnnoyingPlugin} instance
     */
    public AnnoyingPlugin() {
        LOGGER = getLogger();

        // Load Okaeri Configs
        if (!libraryManager.loadLibrary(
                AnnoyingAPILibrary.XSERIES,
                AnnoyingAPILibrary.OKAERI_CONFIGS_YAML_BUKKIT,
                AnnoyingAPILibrary.OKAERI_CONFIGS_SERDES_COMMONS,
                AnnoyingAPILibrary.OKAERI_CONFIGS_SERDES_BUKKIT,
                AnnoyingAPILibrary.OKAERI_CONFIGS_VALIDATOR_OKAERI)) throw new RuntimeException("Failed to load Okaeri Configs");

        configLoader = new ConfigLoader(this);
    }

    /**
     * Called after a plugin is loaded but before it has been enabled.
     * When multiple plugins are loaded, the onLoad() for all plugins is called before any onEnable() is called.
     * <p><b>Do not try to override this method! Override {@link #load()} instead</b>
     *
     * @see #load()
     */
    @Override
    public final void onLoad() {
        // Load required libraries
        if (!libraryManager.loadLibrary(options.pluginOptions.libraries)) {
            log(Level.SEVERE, "&cDisabling &4" + getName() + "&c because required libraries failed to load");
            disablePlugin();
            return;
        }

        selectorManager.registerSelectors();
        loadDataManger(null, false);

        // Run custom onLoad
        load();
    }

    /**
     * Called when the plugin is enabled.
     * <p><b>Do not try to override this method! Override {@link #enable()} instead</b>
     *
     * @see #enable()
     */
    @Override
    public final void onEnable() {
        // Get missing dependencies
        final List<AnnoyingDependency> missingDependencies = new ArrayList<>();
        for (final AnnoyingDependency dependency : options.pluginOptions.dependencies) if (dependency.isNotInstalled() && missingDependencies.stream().noneMatch(dep -> dep.name.equals(dependency.name))) missingDependencies.add(dependency);

        // Download missing dependencies then enable the plugin
        if (!missingDependencies.isEmpty()) {
            log(Level.WARNING, "&6&lMissing dependencies! &eAnnoying API will attempt to download/install them...");
            new AnnoyingDownload(this, missingDependencies).downloadPlugins(this::enablePlugin);
            return;
        }

        // Enable the plugin immediately if there are no missing dependencies
        enablePlugin();
    }

    /**
     * Called when the plugin is disabled
     * <p><b>Do not try to override this method! Override {@link #disable()} instead</b>
     *
     * @see #disable()
     */
    @Override
    public final void onDisable() {
        if (dataManager != null) {
            // Save cache
            if (dataManager.storageConfig.cache.getSaveOn().contains(StorageConfig.Cache.SaveOn.DISABLE)) dataManager.dialect.saveCache();
            // Close connection (if SQL)
            if (dataManager.dialect instanceof SQLDialect) try {
                ((SQLDialect) dataManager.dialect).connection.close();
            } catch (final SQLException e) {
                log(Level.SEVERE, "&cFailed to close the database connection", e);
            }
        }

        // Stats loaders
        for (final Registrable registrable : new HashSet<>(registeredClasses)) if (registrable instanceof StatsProvider<?>) registrable.unregister();

        // Run custom onDisable
        disable();
    }

    /**
     * Called when the plugin is loaded
     *
     * @see #onLoad()
     */
    public void load() {
        // This method is meant to be overridden
    }

    /**
     * Called after dependency checks, start-up messages, and command/listener registration
     *
     * @see #onEnable()
     * @see #enablePlugin()
     */
    public void enable() {
        // This method is meant to be overridden
    }

    /**
     * Called when the plugin is disabled
     *
     * @see #onDisable()
     * @see #disablePlugin()
     */
    public void disable() {
        // This method is meant to be overridden
    }

    /**
     * Called when the plugin is reloaded
     *
     * @see #reloadPlugin()
     */
    public void reload() {
        // This method is meant to be overridden
    }

    /**
     * Plugin enabling stuff
     * <b><p>Do not override this method! Override {@link #enable()} instead</b>
     *
     * @see #enable()
     */
    private void enablePlugin() {
        final String name = getName();

        // Check if they're trying to use API as standalone plugin
        if (name.equals("AnnoyingAPI")) {
            String hideMessage = "";
            try {
                hideMessage = " To hide this message: delete &4" + new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            } catch (final URISyntaxException ignored) {}
            log(Level.SEVERE, "&cDisabling &4AnnoyingAPI&c because it's not meant to be used as a standalone plugin, your plugins will still work!" + hideMessage);
            disablePlugin();
            return;
        }

        // Check if required dependencies are installed
        final StringJoiner joiner = new StringJoiner("&c, &4");
        for (final AnnoyingDependency dependency : options.pluginOptions.dependencies) {
            if (dependency.required && dependency.isNotInstalled()) joiner.add(dependency.name);
        }
        final String missing = joiner.toString();
        if (!missing.isEmpty()) {
            log(Level.SEVERE, "&cDisabling &4" + name + "&c because it's missing required dependencies: &4" + missing);
            disablePlugin();
            return;
        }

        // Register manually-defined Registrables & PAPI expansion
        options.registrationOptions.toRegister.forEach(Registrable::register);
        papiInstalled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiInstalled) options.registrationOptions.getPapiExpansionToRegister().ifPresent(PlaceholderExpansion::register);

        // Get packages for automatic registration
        final Set<String> packages = new HashSet<>(options.registrationOptions.automaticRegistration.packages);
        if (options.registrationOptions.automaticRegistration.addPrimaryPackage) packages.add(getClass().getPackageName());

        // Do automatic registration
        if (!packages.isEmpty()) {
            // Load Reflections library
            libraryManager.loadLibrary(AnnoyingAPILibrary.REFLECTIONS);

            // Register classes
            final Set<Class<? extends Registrable>> ignoredClasses = options.registrationOptions.automaticRegistration.ignoredClasses;
            for (final Class<? extends Registrable> clazz : AnnoyingReflections.getSubTypesOf(packages, Registrable.class)) {
                // Ignore interfaces, abstract classes, and anonymous classes
                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isAnonymousClass()) continue;
                // Ignore classes with @Registrable.Ignore and specific ignored classes
                if (clazz.isAnnotationPresent(Registrable.Ignore.class) || ignoredClasses.contains(clazz)) continue;

                // Register class
                final String className = clazz.getSimpleName();
                log(Level.INFO, "Automatically registering " + className);
                try {
                    clazz.getConstructor(this.getClass()).newInstance(this).register();
                } catch (final Throwable t) {
                    logErrorTrack(Level.WARNING, "&eFailed to register &6" + className, t);
                }
            }
        }

        // Load messages
        MessagesProvider provider = getRegistrable(MessagesProvider.class);
        if (provider == null) {
            provider = new MessagesProvider() {
                private AnnoyingMessages messages;

                @Override @NotNull
                public AnnoyingPlugin getAnnoyingPlugin() {
                    return AnnoyingPlugin.this;
                }

                @Override
                public void setMessages(@NotNull AnnoyingMessages messages) {
                    this.messages = messages;
                }

                @Override @NotNull
                public AnnoyingMessages getMessages() {
                    return messages;
                }
            };
            provider.register();
        }
        provider.setMessages(configLoader.build(options.messagesOptions.builder));

        // Manual stats registration
        registerBStatsManually();
        registerFastStatsManually();

        // Enable/disable interval cache saving (depending on config)
        if (dataManager != null) dataManager.toggleIntervalCacheSaving();

        // Get start message colors
        final AnnoyingMessages annoyingMessages = getAnnoyingMessages();
        final String primaryColorString = annoyingMessages.plugin.global_placeholders.get("p");
        final String primaryColor = primaryColorString != null ? BukkitUtility.color(primaryColorString) : ChatColor.AQUA.toString();
        final String secondaryColorString = annoyingMessages.plugin.global_placeholders.get("s");
        final String secondaryColor = secondaryColorString != null ? BukkitUtility.color(secondaryColorString) : ChatColor.DARK_AQUA.toString();

        // Get start messages
        final PluginDescriptionFile description = getDescription();
        final String nameVersion = name + " v" + description.getVersion();
        final String authors = "By " + String.join(", ", description.getAuthors());
        final StringBuilder lineBuilder = new StringBuilder(secondaryColor);
        final int lineLength = Math.max(nameVersion.length(), authors.length());
        lineBuilder.append("-".repeat(lineLength));
        final String line = lineBuilder.toString();

        // Send start messages
        log(Level.INFO, line);
        log(Level.INFO, primaryColor + nameVersion);
        log(Level.INFO, primaryColor + authors);
        log(Level.INFO, line);

        // Check for updates
        updateChecker = new UpdateChecker(this, options.pluginOptions.updatePlatforms);
        updateChecker.checkUpdate();

        // Custom onEnable
        enable();
    }

    /**
     * Runs {@link PluginManager#disablePlugin(Plugin)} with {@code this} as the plugin
     * <br><b>Do not override this method! Override {@link #disable()} instead</b>
     *
     * @see #disable()
     */
    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(this);
    }

    /**
     * Reloads the plugin (messages, etc...). This will not trigger {@link #onLoad()} or {@link #onEnable()}
     * <p>This is not run automatically (such as {@link #onEnable()} and {@link #onDisable()}), it is to be used manually by the plugin itself (ex: in a {@code /reload} command)
     * <p><b>Do not override this method! Override {@link #reload()} instead</b>
     *
     * @see #reload()
     */
    public void reloadPlugin() {
        // Reload messages
        final MessagesProvider provider = getRegistrable(MessagesProvider.class);
        if (provider != null) provider.getMessages().load(true);

        // Save cache if new config has RELOAD in save-on OR cache used to be enabled but now disabled
        StorageConfig storageConfig = null;
        boolean saveCache = false;
        if (options.dataOptions.enabled) {
            storageConfig = newStorageConfig();
            if (storageConfig == null) throw new RuntimeException("Failed to load storage config");
            saveCache = storageConfig.cache.getSaveOn().contains(StorageConfig.Cache.SaveOn.RELOAD) || (dataManager != null && dataManager.storageConfig.cache.enabled && !storageConfig.cache.enabled);
        }
        // Load data manager
        loadDataManger(storageConfig, saveCache);
        if (dataManager != null) dataManager.toggleIntervalCacheSaving();

        // Custom reload
        reload();
    }

    /**
     * Get a {@link Registrable} by its super-class or exact class
     *
     * @param   clazz   The class of the {@link Registrable} to get
     *
     * @return  The {@link Registrable} if it exists, or {@code null} if it doesn't
     *
     * @param   <T> The type of the {@link Registrable}
     */
    @Nullable
    public <T extends Registrable> T getRegistrable(@NotNull Class<T> clazz) {
        for (final Registrable registrable : registeredClasses) if (clazz.isAssignableFrom(registrable.getClass())) return (T) registrable;
        return null;
    }

    /**
     * Get a {@link Registrable} by its exact class
     *
     * @param   clazz   The exact class of the {@link Registrable} to get
     *
     * @return  The {@link Registrable} if it exists, or {@code null} if it doesn't
     *
     * @param   <T> The type of the {@link Registrable}
     */
    @Nullable
    public <T extends Registrable> T getRegistrableExact(@NotNull Class<T> clazz) {
        for (final Registrable registrable : registeredClasses) if (registrable.getClass().equals(clazz)) return (T) registrable;
        return null;
    }

    /**
     * Unregisters all {@link Registrable}s in {@link #registeredClasses}
     */
    public void unregisterClasses() {
        new HashSet<>(registeredClasses).forEach(Registrable::unregister);
    }

    private void registerBStatsManually() {
        // Stop if no manual loader/ID provided
        if (options.statsOptions.bStats.loader == null && options.statsOptions.bStats.id == null) return;

        // Stop if explicit provider already registered
        if (getRegistrable(BStatsProvider.class) != null) return;

        // Register manually
        try {
            new BStatsProvider<>(this) {
                @Override @NotNull
                public BStatsLoader createLoader() {
                    if (options.statsOptions.bStats.loader != null) try {
                        return options.statsOptions.bStats.loader.getConstructor(AnnoyingPlugin.this.getClass()).newInstance(AnnoyingPlugin.this);
                    } catch (final Exception t) {
                        throw new RuntimeException("[MANUAL] Failed to create bStats loader", t);
                    }

                    return new BStatsLoader() {
                        @Override @NotNull
                        public AnnoyingPlugin getAnnoyingPlugin() {
                            return AnnoyingPlugin.this;
                        }

                        @Override @NotNull
                        public Integer getId() {
                            if (options.statsOptions.bStats.id == null) throw new RuntimeException("[MANUAL] bStats ID is null");
                            return options.statsOptions.bStats.id;
                        }
                    };
                }
            }.register();
        } catch (final Exception e) {
            logErrorTrack(Level.WARNING, "&eFailed to register &6bStats&e metrics", e);
        }
    }

    private void registerFastStatsManually() {
        // Stop if no manual loader/ID provided
        if (options.statsOptions.fastStats.loader == null && options.statsOptions.fastStats.id == null) return;

        // Stop if explicit provider already registered
        if (getRegistrable(FastStatsProvider.class) != null) return;

        // Register manually
        try {
            new FastStatsProvider<>(this) {
                @Override @NotNull
                public FastStatsLoader createLoader() {
                    if (options.statsOptions.fastStats.loader != null) try {
                        return options.statsOptions.fastStats.loader.getConstructor(AnnoyingPlugin.this.getClass()).newInstance(AnnoyingPlugin.this);
                    } catch (final Exception t) {
                        throw new RuntimeException("[MANUAL] Failed to create FastStats loader", t);
                    }

                    return new FastStatsLoader() {
                        @Override @NotNull
                        public AnnoyingPlugin getAnnoyingPlugin() {
                            return AnnoyingPlugin.this;
                        }

                        @Override @NotNull
                        public String getId() {
                            if (options.statsOptions.fastStats.id == null) throw new RuntimeException("[MANUAL] FastStats ID is null");
                            return options.statsOptions.fastStats.id;
                        }
                    };
                }
            }.register();
        } catch (final Exception e) {
            log(Level.WARNING, "&eFailed to register &6FastStats&e metrics", e);
        }
    }

    @NotNull
    public MessagesProvider getMessagesProvider() {
        return Objects.requireNonNull(getRegistrable(MessagesProvider.class), "Messages not loaded yet");
    }

    /**
     * Not overrideable as it would cause a {@link NoClassDefFoundError} for {@link OkaeriConfig} in consumers!
     */
    @NotNull
    public final AnnoyingMessages getAnnoyingMessages() {
        return getMessagesProvider().getMessages();
    }

    /**
     * Parses all PlaceholderAPI placeholders in a message
     *
     * @param   player  the {@link OfflinePlayer} to parse the placeholders for (or {@code null} if none)
     * @param   message the message to parse
     *
     * @return          the parsed message
     */
    @NotNull
    public String parsePapiPlaceholders(@Nullable OfflinePlayer player, @Nullable String message) {
        if (message == null) return "null";
        return papiInstalled ? PlaceholderAPI.setPlaceholders(player, message) : message;
    }

    @Nullable
    public StorageConfig newStorageConfig(@NotNull String fileName) {
        return configLoader.build(configBuilder -> configBuilder
                .config(new StorageConfig(this))
                .file(fileName)
                .internalStateMigrations(new S0001_Cache_interval_ticks_to_duration()));
    }

    @Nullable
    public StorageConfig newStorageConfig() {
        return newStorageConfig("storage.yml");
    }

    /**
     * Attempts to load the {@link #dataManager}, catching any exceptions and logging them
     * <br>If {@code storage-new.yml} exists, it will attempt to migrate the data from {@code storage.yml} to {@code storage-new.yml} using {@link DataManager#attemptDatabaseMigration()}
     *
     * @param   storageConfig   the {@link StorageConfig} to load the data manager with. If {@code null}, {@code storage.yml} will be used
     * @param   saveCache       whether to save the cache before loading the data manager <b>(data may be lost if {@code false})</b>
     */
    public void loadDataManger(@Nullable StorageConfig storageConfig, boolean saveCache) {
        // Check if a manager is already loaded
        if (dataManager != null) {
            // Save cache
            if (saveCache) dataManager.dialect.saveCache();
            // Close previous connection
            if (dataManager.dialect instanceof SQLDialect) try {
                ((SQLDialect) dataManager.dialect).connection.close();
            } catch (final SQLException e) {
                log(Level.SEVERE, "&cFailed to close the database connection, it's recommended to restart the server!", e);
            }
            // Stop cache saving task
            if (dataManager.cacheSavingTask != null) dataManager.cacheSavingTask.cancel();
        }

        // Cancel if data is disabled
        if (!options.dataOptions.enabled) {
            dataManager = null;
            return;
        }

        // Get storage config
        if (storageConfig == null) {
            storageConfig = newStorageConfig();
            if (storageConfig == null) throw new RuntimeException("Failed to load storage config");
        }

        // Connect to database
        try {
            dataManager = new DataManager(storageConfig);
        } catch (final ConnectionException e) {
            dataManager = null;
            log(Level.SEVERE, "&4storage.yml &8|&c Failed to connect to database! URL: '&4" + e.url + "&c' Properties: &4" + e.getPropertiesRedacted(), e);
            return;
        }

        // Attempt database migration
        dataManager = dataManager.attemptDatabaseMigration();
        // Create tables/columns
        if (dataManager.dialect instanceof SQLDialect) {
            final Map<String, Set<String>> tablesCopy = new HashMap<>(options.dataOptions.tables);

            // Remove entities table if it has no custom columns
            final Set<String> entitiesTable = tablesCopy.get(EntityData.TABLE_NAME);
            if (entitiesTable != null && entitiesTable.size() == 1) tablesCopy.remove(EntityData.TABLE_NAME);

            ((SQLDialect) dataManager.dialect).createTablesKeys(tablesCopy);
        }
    }

    /**
     * Gets a {@link Relocation} for the specified package
     *
     * @param   from    the package to relocate
     * @param   name    the name of the module being relocated ({@link #getLibsPackage()} + {@code name})
     *
     * @return          the relocation
     */
    @NotNull
    public Relocation getRelocation(@NotNull String from, @NotNull String name) {
        return new Relocation(from, getLibsPackage() + name);
    }

    /**
     * Gets a {@link Relocation} for the specified package
     *
     * @param   from    the package to relocate
     *
     * @return          the relocation
     */
    @NotNull
    public Relocation getRelocation(@NotNull String from) {
        return getRelocation(from, from.substring(from.lastIndexOf("{}") + 2).toLowerCase().replaceAll("[^a-z0-9._{}]", ""));
    }

    /**
     * Gets the package path for relocated libraries
     *
     * @return  the path
     */
    @NotNull
    public String getLibsPackage() {
        return getClass().getPackage().getName() + "{}libs{}";
    }

    /**
     * Logs a message with the specified level and throwable to the console
     *
     * @param   level       the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message     the message to log
     * @param   throwable   the throwable to log
     */
    public static void log(@Nullable Level level, @Nullable Object message, @Nullable Throwable throwable) {
        if (level == null) level = Level.INFO;
        final String messageString = String.valueOf(message);
        LOGGER.log(level,
                // Only color the message if the server version is between 1.12 and 1.20
                MINECRAFT_VERSION.isGreaterThanOrEqualTo(1, 12, 0) && MINECRAFT_VERSION.isLessThanOrEqualTo(1, 20, 0)
                        ? BukkitUtility.color(messageString)
                        : BukkitUtility.stripUntranslatedColor(messageString),
                throwable);
    }

    /**
     * Calls {@link #log(Level, Object, Throwable)} with {@code null} as the {@link Throwable throwable}
     *
     * @param   level   the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message the message to log
     */
    public static void log(@Nullable Level level, @Nullable Object message) {
        log(level, message, null);
    }

    /**
     * Calls {@link #log(Level, Object, Throwable)} with {@code null} as the {@link Level level} and {@link Throwable throwable}
     *
     * @param   message the message to log
     */
    public static void log(@Nullable Object message) {
        log(null, message, null);
    }

    public void logErrorTrack(@Nullable Level level, @Nullable Object message, @Nullable Throwable throwable) {
        // Log
        log(level, message, throwable);

        // FastStats error tracking
        final FastStatsProvider<?> fastStatsProvider = getRegistrable(FastStatsProvider.class);
        if (fastStatsProvider == null || fastStatsProvider.loader == null) return;
        if (throwable != null) {
            fastStatsProvider.loader.errorTracker.trackError(throwable);
        } else if (message != null) {
            fastStatsProvider.loader.errorTracker.trackError(message.toString());
        }
    }

    public void logErrorTrack(@Nullable Level level, @Nullable Object message) {
        logErrorTrack(level, message, null);
    }

    public void logErrorTrack(@Nullable Object message) {
        logErrorTrack(null, message, null);
    }

    /**
     * Replaces all instances of {@code {}} in a string with {@code .}
     * <br>Used for replacing brackets in package names
     *
     * @param   string  the string to replace brackets in
     *
     * @return          the string with brackets replaced
     */
    @NotNull
    public static String replaceBrackets(@NotNull String string) {
        return string.replace("{}", ".");
    }
}
