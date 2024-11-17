package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import net.byteflux.libby.relocation.Relocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.cooldown.CooldownManager;
import xyz.srnyx.annoyingapi.data.EntityData;
import xyz.srnyx.annoyingapi.storage.ConnectionException;
import xyz.srnyx.annoyingapi.storage.DataManager;
import xyz.srnyx.annoyingapi.storage.StorageConfig;
import xyz.srnyx.annoyingapi.storage.dialects.sql.SQLDialect;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.events.AdvancedPlayerMoveEvent;
import xyz.srnyx.annoyingapi.events.PlayerDamageByPlayerEvent;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.library.AnnoyingLibraryManager;
import xyz.srnyx.annoyingapi.library.RuntimeLibrary;
import xyz.srnyx.annoyingapi.options.*;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;

import xyz.srnyx.javautilities.MapGenerator;
import xyz.srnyx.javautilities.objects.SemanticVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Represents a plugin using Annoying API
 */
@SuppressWarnings("EmptyMethod")
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * The {@link Logger} for the plugin
     * <br>Uses temporary initialization until the {@link AnnoyingPlugin plugin} is constructed (loaded)
     */
    @NotNull public static Logger LOGGER = new Logger("AnnoyingAPI - ?", null) {
        @Override
        public void log(@NotNull LogRecord logRecord) {
            logRecord.setMessage("[AnnoyingAPI - ?] " + logRecord.getMessage());
            super.log(logRecord);
        }
    };
    /**
     * The version of Annoying API the plugin is using
     */
    @NotNull public static final SemanticVersion ANNOYING_API_VERSION = new SemanticVersion(5, 1, 3);
    /**
     * The Minecraft version the server is running
     */
    @NotNull public static final SemanticVersion MINECRAFT_VERSION = new SemanticVersion(Bukkit.getVersion().split("MC: ")[1].split("\\)")[0]);

    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = AnnoyingOptions.load(getResource("plugin.yml"));
    /**
     * The {@link AnnoyingLibraryManager} for the plugin to manage {@link RuntimeLibrary libraries}
     */
    @NotNull public final AnnoyingLibraryManager libraryManager = new AnnoyingLibraryManager(this, "libs");
    /**
     * Wrapper for bStats
     */
    @Nullable public AnnoyingStats stats;
    /**
     * The {@link AnnoyingResource} that contains the plugin's messages
     */
    @Nullable public AnnoyingResource messages;
    /**
     * {@link ChatColor} aliases for the plugin from the messages file ({@link MessagesOptions.MessageKeys#globalPlaceholders})
     *
     * @see MessagesOptions.MessageKeys#globalPlaceholders
     */
    @NotNull public final Map<String, String> globalPlaceholders = new HashMap<>();
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
     * The {@link CooldownManager} for the plugin
     */
    @NotNull public final CooldownManager cooldownManager = new CooldownManager();
    /**
     * Whether PlaceholderAPI is installed
     */
    public boolean papiInstalled = false;

    /**
     * Constructs a new {@link AnnoyingPlugin} instance
     */
    public AnnoyingPlugin() {
        LOGGER = getLogger();
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
        loadMessages();
        loadDataManger(false);
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
            if (dataManager.storageConfig.cache.saveOn.contains(StorageConfig.SaveOn.DISABLE)) dataManager.dialect.saveCache();
            // Close connection (if SQL)
            if (dataManager.dialect instanceof SQLDialect) try {
                ((SQLDialect) dataManager.dialect).connection.close();
            } catch (final SQLException e) {
                log(Level.SEVERE, "&cFailed to close the database connection", e);
            }
        }

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
        // Check if required dependencies are installed
        final String missing = options.pluginOptions.dependencies.stream()
                .filter(dependency -> dependency.required && dependency.isNotInstalled())
                .map(dependency -> dependency.name)
                .collect(Collectors.joining("&c, &4"));
        if (!missing.isEmpty()) {
            log(Level.SEVERE, "&cDisabling &4" + getName() + "&c because it's missing required dependencies: &4" + missing);
            disablePlugin();
            return;
        }

        // Enable bStats
        if (new AnnoyingResource(this, options.bStatsOptions.fileName, options.bStatsOptions.fileOptions).getBoolean(options.bStatsOptions.toggleKey)) {
            libraryManager.loadLibrary(RuntimeLibrary.BSTATS_BASE);
            libraryManager.loadLibrary(RuntimeLibrary.BSTATS_BUKKIT);
            stats = new AnnoyingStats(this);
        }

        // Get start message colors
        final String primaryColorString = globalPlaceholders.get("p");
        final String primaryColor = primaryColorString != null ? BukkitUtility.color(primaryColorString) : ChatColor.AQUA.toString();
        final String secondaryColorString = globalPlaceholders.get("s");
        final String secondaryColor = secondaryColorString != null ? BukkitUtility.color(secondaryColorString) : ChatColor.DARK_AQUA.toString();

        // Get start messages
        final PluginDescriptionFile description = getDescription();
        final String nameVersion = getName() + " v" + description.getVersion();
        final String authors = "By " + String.join(", ", description.getAuthors());
        final StringBuilder lineBuilder = new StringBuilder(secondaryColor);
        final int lineLength = Math.max(nameVersion.length(), authors.length());
        for (int i = 0; i < lineLength; i++) lineBuilder.append("-");
        final String line = lineBuilder.toString();

        // Send start messages
        log(Level.INFO, line);
        log(Level.INFO, primaryColor + nameVersion);
        log(Level.INFO, primaryColor + authors);
        log(Level.INFO, line);

        // Check for updates
        checkUpdate();

        // Register manually-defined Registrables & PAPI expansion
        options.registrationOptions.toRegister.forEach(Registrable::register);
        papiInstalled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiInstalled) options.registrationOptions.getPapiExpansionToRegister().ifPresent(PlaceholderExpansion::register);

        // Automatic registration
        final Set<String> packages = options.registrationOptions.automaticRegistration.packages;
        if (!packages.isEmpty()) {
            // Load Javassist and Reflections libraries
            libraryManager.loadLibrary(RuntimeLibrary.JAVASSIST);
            libraryManager.loadLibrary(RuntimeLibrary.REFLECTIONS);

            // Register classes
            final Set<Class<? extends Registrable>> ignoredClasses = options.registrationOptions.automaticRegistration.ignoredClasses;
            AnnoyingReflections.getSubTypesOf(packages, Registrable.class).stream()
                    .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isAnnotationPresent(Registrable.Ignore.class) && !ignoredClasses.contains(clazz))
                    .forEach(clazz -> {
                        try {
                            clazz.getConstructor(this.getClass()).newInstance(this).register();
                        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            log(Level.WARNING, "&eFailed to register &6" + clazz.getSimpleName());
                            e.printStackTrace();
                        }
                    });
        }

        // Enable/disable interval cache saving (depending on config)
        if (dataManager != null) dataManager.toggleIntervalCacheSaving();

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
     * Reloads the plugin ({@link #messages}, etc...). This will not trigger {@link #onLoad()} or {@link #onEnable()}
     * <p>This is not run automatically (such as {@link #onEnable()} and {@link #onDisable()}), it is to be used manually by the plugin itself (ex: in a {@code /reload} command)
     * <p><b>Do not override this method! Override {@link #reload()} instead</b>
     *
     * @see #reload()
     */
    public void reloadPlugin() {
        loadMessages();
        loadDataManger(dataManager != null && dataManager.storageConfig.cache.saveOn.contains(StorageConfig.SaveOn.RELOAD));
        if (dataManager != null) dataManager.toggleIntervalCacheSaving();
        reload();
    }

    /**
     * Loads the messages.yml file to {@link #messages} and {@link #globalPlaceholders}
     */
    public void loadMessages() {
        messages = new AnnoyingResource(this, options.messagesOptions.fileName, options.messagesOptions.fileOptions);
        // globalPlaceholders
        globalPlaceholders.clear();
        final ConfigurationSection section = messages.getConfigurationSection(options.messagesOptions.keys.globalPlaceholders);
        if (section != null) section.getKeys(false).forEach(key -> globalPlaceholders.put(key, section.getString(key)));
    }

    /**
     * Gets a string from {@link #messages} with the specified key
     *
     * @param   key the key of the string
     *
     * @return      the string, or the {@code key} if {@link #messages} is {@code null} or the string is not found
     */
    @NotNull
    public String getMessagesString(@NotNull String key) {
        return messages != null ? messages.getString(key, key) : key;
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

    /**
     * Unregisters all {@link Registrable}s in {@link #registeredClasses}
     */
    public void unregisterClasses() {
        new HashSet<>(registeredClasses).forEach(Registrable::unregister);
    }

    /**
     * Runs {@link AnnoyingUpdate#checkUpdate()} with {@link PluginOptions#updatePlatforms}
     *
     * @see AnnoyingUpdate
     */
    public void checkUpdate() {
        new AnnoyingUpdate(this, options.pluginOptions.updatePlatforms).checkUpdate();
    }

    /**
     * Attempts to load the {@link #dataManager}, catching any exceptions and logging them
     * <br>If {@code storage-new.yml} exists, it will attempt to migrate the data from {@code storage.yml} to {@code storage-new.yml} using {@link DataManager#attemptDatabaseMigration()}
     *
     * @param   saveCache   whether to save the cache before loading the data manager
     *                      <br><i>Data may be lost if {@code false}!</i>
     */
    public void loadDataManger(boolean saveCache) {
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
        }

        // Cancel if data is disabled
        if (!options.dataOptions.enabled) {
            dataManager = null;
            return;
        }

        // Connect to database
        try {
            dataManager = new DataManager(new StorageConfig(new AnnoyingResource(this, "storage.yml")));
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
     * Attempt to run a task asynchronously if the plugin is enabled, otherwise run it synchronously
     *
     * @param   runnable    the task to run
     *
     * @return              the {@link BukkitTask} if the task was run asynchronously, otherwise {@link Optional#empty()}
     */
    @NotNull @SuppressWarnings("UnusedReturnValue")
    public Optional<BukkitTask> attemptAsync(@NotNull Runnable runnable) {
        try {
            return Optional.of(Bukkit.getScheduler().runTaskAsynchronously(this, runnable));
        } catch (final IllegalPluginAccessException e) {
            runnable.run();
            return Optional.empty();
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
        // Only color the message if the server version is between 1.12 and 1.20
        LOGGER.log(level, MINECRAFT_VERSION.isGreaterThanOrEqualTo(1, 12, 0) && MINECRAFT_VERSION.isLessThanOrEqualTo(1, 20, 0) ? BukkitUtility.color(message) : BukkitUtility.stripUntranslatedColor(String.valueOf(message)), throwable);
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
