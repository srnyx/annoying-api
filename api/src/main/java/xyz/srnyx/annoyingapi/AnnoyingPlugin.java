package xyz.srnyx.annoyingapi;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.apache.commons.lang.StringUtils;

import org.bstats.bukkit.Metrics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.events.EventHandlers;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.utility.AnnoyingUtility;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Represents a plugin using the API
 */
@SuppressWarnings("EmptyMethod")
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * The Minecraft version the server is running
     */
    @NotNull public static final MinecraftVersion MINECRAFT_VERSION = new MinecraftVersion(Bukkit.getVersion().split("MC: ")[1].split("\\)")[0]);

    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = new AnnoyingOptions();
    /**
     * The {@link Metrics bStats} instance for the plugin
     */
    @Nullable public Metrics bStats;
    /**
     * The {@link AnnoyingResource} that contains the plugin's messages
     */
    @Nullable public AnnoyingResource messages;
    /**
     * {@link ChatColor} aliases for the plugin from the messages file ({@link AnnoyingOptions#globalPlaceholders})
     *
     * @see AnnoyingOptions#globalPlaceholders
     */
    @NotNull public final Map<String, String> globalPlaceholders = new HashMap<>();
    /**
     * Set of registered {@link AnnoyingCommand}s by the plugin
     */
    @NotNull public final Set<AnnoyingCommand> registeredCommands = new HashSet<>();
    /**
     * Set of registered {@link AnnoyingListener}s by the plugin
     */
    @NotNull public final Set<AnnoyingListener> registeredListeners = new HashSet<>();
    /**
     * Stores the cooldowns for each player/type
     */
    @NotNull public final Map<UUID, Map<AnnoyingCooldown.CooldownType, Long>> cooldowns = new HashMap<>();
    /**
     * Whether PlaceholderAPI is installed
     */
    public boolean papiInstalled = false;

    /**
     * Constructs a new {@link AnnoyingPlugin} instance. Registers event handlers for custom events
     */
    public AnnoyingPlugin() {
        options.listenersToRegister.add(new EventHandlers(this));
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
        for (final AnnoyingDependency dependency : options.dependencies) if (dependency.isNotInstalled() && missingDependencies.stream().noneMatch(dep -> dep.name.equals(dependency.name))) missingDependencies.add(dependency);

        // Download missing dependencies then enable the plugin
        if (!missingDependencies.isEmpty()) {
            log(Level.WARNING, "&6&lMissing dependencies! &eAnnoyingAPI will attempt to download/install them...");
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
        final String missing = options.dependencies.stream()
                .filter(dependency -> dependency.required && dependency.isNotInstalled())
                .map(dependency -> dependency.name)
                .collect(Collectors.joining("&c, &4"));
        if (missing.length() != 0) {
            log(Level.SEVERE, "&cDisabling " + getName() + " because it's missing required dependencies: &4" + missing);
            disablePlugin();
            return;
        }

        // Enable bStats
        if (new AnnoyingResource(this, options.bStatsFileName, options.bStatsOptions).getBoolean("enabled")) {
            new Metrics(this, 18281); // API
            if (options.bStatsId != null) bStats = new Metrics(this, options.bStatsId); // Plugin
        }

        // Get start message colors
        final String primaryColorString = globalPlaceholders.get("p");
        final String primaryColor = primaryColorString != null ? AnnoyingUtility.color(primaryColorString) : ChatColor.AQUA.toString();
        final String secondaryColorString = globalPlaceholders.get("s");
        final String secondaryColor = secondaryColorString != null ? AnnoyingUtility.color(secondaryColorString) : ChatColor.DARK_AQUA.toString();

        // Send start messages
        final String name = getName() + " v" + getDescription().getVersion();
        final String authors = "By " + String.join(", ", getDescription().getAuthors());
        final String line = secondaryColor + StringUtils.repeat("-", Math.max(name.length(), authors.length()));
        log(Level.INFO, line);
        log(Level.INFO, primaryColor + name);
        log(Level.INFO, primaryColor + authors);
        log(Level.INFO, line);

        // Check for updates
        checkUpdate();

        // Register commands/events/PAPI expansion
        options.commandsToRegister.forEach(AnnoyingCommand::register);
        options.listenersToRegister.forEach(AnnoyingListener::register);
        papiInstalled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papiInstalled) {
            final PlaceholderExpansion expansion = options.getPapiExpansionToRegister();
            if (expansion != null) expansion.register();
        }

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
        reload();
    }

    /**
     * Loads the messages.yml file to {@link #messages} and {@link #globalPlaceholders}
     */
    public void loadMessages() {
        messages = new AnnoyingResource(this, options.messagesFileName, options.messagesOptions);
        // globalPlaceholders
        globalPlaceholders.clear();
        final ConfigurationSection section = messages.getConfigurationSection(options.globalPlaceholders);
        if (section != null) section.getKeys(false).forEach(key -> globalPlaceholders.put(key, section.getString(key)));
    }

    /**
     * Disables the plugin. Unregisters commands/listeners, cancels tasks, and then runs {@link PluginManager#disablePlugin(Plugin)}
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public void disablePlugin() {
        options.commandsToRegister.forEach(AnnoyingCommand::unregister);
        options.listenersToRegister.forEach(AnnoyingListener::unregister);
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    /**
     * Logs a message to the console
     *
     * @param   level   the level of the message. If {@code null}, {@link Level#INFO} will be used
     * @param   message the message to log
     */
    public void log(@Nullable Level level, @NotNull String message) {
        if (level == null) level = Level.INFO;
        getLogger().log(level, AnnoyingUtility.color(message));
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
        if (messages == null) return key;
        return messages.getString(key, key);
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
        if (!papiInstalled) return message;
        return PlaceholderAPI.setPlaceholders(player, message);
    }

    /**
     * Unregisters all {@link AnnoyingListener}s in {@link #registeredListeners}
     */
    public void unregisterListeners() {
        new HashSet<>(registeredListeners).forEach(AnnoyingListener::unregister);
    }

    /**
     * Unregisters all {@link AnnoyingCommand}s in {@link #registeredCommands}
     */
    public void unregisterCommands() {
        new HashSet<>(registeredCommands).forEach(AnnoyingCommand::unregister);
    }

    /**
     * <b>This is not done, this won't do anything until it's completed!</b>
     * <p>Checks if an update is available for the plugin
     * <p>If an update is available, a message will be sent to the console
     *
     * @see AnnoyingUpdate
     */
    public void checkUpdate() {
        final String name = getName();
        final AnnoyingUpdate update = new AnnoyingUpdate(this, name, getDescription().getVersion(), options.updatePlatforms);
        final AnnoyingUpdate.Version latestVersion = update.latestVersion;
        if (latestVersion != null && update.isUpdateAvailable()) log(Level.WARNING, new AnnoyingMessage(this, options.updateAvailable)
                .replace("%plugin%", name)
                .replace("%current%", getDescription().getVersion())
                .replace("%new%", latestVersion.versionString)
                .toString());
    }
}
