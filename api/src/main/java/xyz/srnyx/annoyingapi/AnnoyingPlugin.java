package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.data.ConnectionException;
import xyz.srnyx.annoyingapi.data.DataManager;
import xyz.srnyx.annoyingapi.data.EntityData;
import xyz.srnyx.annoyingapi.data.StringData;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.events.EventHandlers;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.options.AnnoyingOptions;
import xyz.srnyx.annoyingapi.options.MessagesOptions;
import xyz.srnyx.annoyingapi.options.PluginOptions;
import xyz.srnyx.annoyingapi.parents.Registrable;
import xyz.srnyx.annoyingapi.utility.BukkitUtility;

import xyz.srnyx.javautilities.objects.SemanticVersion;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
     * The Minecraft version the server is running
     */
    @NotNull public static final SemanticVersion MINECRAFT_VERSION = new SemanticVersion(Bukkit.getVersion().split("MC: ")[1].split("\\)")[0]);

    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = AnnoyingOptions.load(new InputStreamReader(getResource("plugin.yml")));
    /**
     * The {@link Metrics bStats} instance for the plugin
     */
    @Nullable public Metrics bStats;
    /**
     * The {@link AnnoyingResource} that contains the plugin's messages
     */
    @Nullable public AnnoyingResource messages;
    /**
     * The {@link DataManager} for the plugin
     */
    @Nullable public DataManager dataManager;
    /**
     * {@link ChatColor} aliases for the plugin from the messages file ({@link MessagesOptions.MessageKeys#globalPlaceholders})
     *
     * @see MessagesOptions.MessageKeys#globalPlaceholders
     */
    @NotNull public final Map<String, String> globalPlaceholders = new HashMap<>();
    /**
     */
    /**
     * Set of registered {@link Registrable}s by the plugin
     */
    @NotNull public final Set<Registrable> registeredClasses = new HashSet<>();
    @NotNull public final CooldownManager cooldownManager = new CooldownManager(this);
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
        loadDataManger();
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
        disable();
    }

    /**
     * Called when the plugin is loaded
     */
    public void load() {
        // This method is meant to be overridden
    }

    /**
     * Called after dependency checks, start-up messages, and command/listener registration.
     */
    public void enable() {
        // This method is meant to be overridden
    }

    /**
     * Called when the plugin is disabled
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
     * <b><p>Do not try to override this method! Override {@link #enable()} instead</b>
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
            final Metrics apiMetrics = new Metrics(this, 18281); // API
            apiMetrics.addCustomChart(new SimplePie("plugins", this::getName));
            apiMetrics.addCustomChart(new SimplePie("storage_method", () -> dataManager == null ? "N/A" : dataManager.storageConfig.method.name()));
            if (options.bStatsOptions.id != null) bStats = new Metrics(this, options.bStatsOptions.id); // Plugin
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

        // Register custom events, manually-defined Registrables, & PAPI expansion
        if (options.registrationOptions.registerCustomEvents) new EventHandlers(this).register();
        options.registrationOptions.toRegister.forEach(Registrable::register);
        papiInstalled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiInstalled) {
            final PlaceholderExpansion expansion = options.registrationOptions.getPapiExpansionToRegister();
            if (expansion != null) expansion.register();
        }

        // Automatic registration
        final Set<Class<? extends Registrable>> ignoredClasses = options.registrationOptions.automaticRegistration.ignoredClasses;
        new AnnoyingReflections(options.registrationOptions.automaticRegistration.packages).getSubTypesOf(Registrable.class).stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()) && !ignoredClasses.contains(clazz))
                .forEach(clazz -> {
                    try {
                        clazz.getConstructor(this.getClass()).newInstance(this).register();
                    } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log(Level.WARNING, "&eFailed to register &6" + clazz.getSimpleName());
                        e.printStackTrace();
                    }
                });

        // Custom onEnable
        enable();
    }

    /**
     * Reloads the plugin ({@link #messages}, etc...). This will not trigger {@link #onLoad()} or {@link #onEnable()}
     * <p>This is not run automatically (such as {@link #onEnable()} and {@link #onDisable()}), it is to be used manually by the plugin itself (ex: in a {@code /reload} command)
     * <p><b>Do not try to override this method! Override {@link #reload()} instead</b>
     *
     * @see #reload()
     */
    public void reloadPlugin() {
        loadMessages();
        loadDataManger();
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
     * Disables the plugin. Unregisters commands/listeners, cancels tasks, and then runs {@link PluginManager#disablePlugin(Plugin)}
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public void disablePlugin() {
        new HashSet<>(registeredClasses).forEach(Registrable::unregister);
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getPluginManager().disablePlugin(this);
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
     */
    public void loadDataManger() {
        try {
            dataManager = options.dataOptions.enabled ? new DataManager(this) : null;
        } catch (final ConnectionException e) {
            dataManager = null;
            AnnoyingPlugin.log(Level.WARNING, "Failed to connect to database! URL: '" + e.url + "' Properties: " + e.properties, e);
        }
    }

    /**
     * Logs a message with the specified level and throwable to the console
     *
     * @param   level       the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message     the message to log
     * @param   throwable   the throwable to log
     */
    public static void log(@Nullable Level level, @Nullable String message, @Nullable Throwable throwable) {
        if (level == null) level = Level.INFO;
        LOGGER.log(level, BukkitUtility.color(message), throwable);
    }

    /**
     * Calls {@link #log(Level, String, Throwable)} with {@code null} as the {@link Throwable throwable}
     *
     * @param   level   the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message the message to log
     */
    public static void log(@Nullable Level level, @Nullable String message) {
        log(level, message, null);
    }

    /**
     * Calls {@link #log(Level, String, Throwable)} with {@code null} as the {@link Level level} and {@link Throwable throwable}
     *
     * @param   message the message to log
     */
    public static void log(@Nullable String message) {
        log(null, message, null);
    }
}
