package xyz.srnyx.annoyingapi;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.command.AnnoyingCommand;
import xyz.srnyx.annoyingapi.dependency.AnnoyingCommandRegister;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Represents a plugin using the API
 */
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * Instance of {@link AnnoyingCommandRegister}, used to other plugins' register commands
     */
    @NotNull public final AnnoyingCommandRegister commandRegister = new AnnoyingCommandRegister();

    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = new AnnoyingOptions();

    /**
     * The {@link AnnoyingResource} that contains the plugin's messages
     */
    @Nullable public AnnoyingResource messages;

    /**
     * Stores the cooldowns for each player/type
     */
    @NotNull public final Map<UUID, Map<AnnoyingCooldown.CooldownType, Long>> cooldowns = new HashMap<>();

    /**
     * Constructs a new {@link AnnoyingPlugin} instance
     * <p><i>Only exists to give the constructor a Javadoc</i>
     */
    public AnnoyingPlugin() {
        // Only exists to give the constructor a Javadoc
    }

    /**
     * Called after a plugin is loaded but before it has been enabled.
     * When multiple plugins are loaded, the onLoad() for all plugins is called before any onEnable() is called.
     * <p>Do not try to override this method! Override {@link #load()} instead
     *
     * @see #load()
     */
    @Override
    public final void onLoad() {
        messages = new AnnoyingResource(this, options.messagesFileName);
        options.prefix = getMessagesString(options.prefix);
        options.splitterJson = getMessagesString(options.splitterJson);
        options.splitterPlaceholder = getMessagesString(options.splitterPlaceholder);

        // Custom onLoad
        load();
    }

    /**
     * Called when the plugin is enabled.
     * <p>Do not try to override this method! Override {@link #enable()} instead
     *
     * @see #enable()
     */
    @Override
    public final void onEnable() {
        // Get missing dependencies
        final List<AnnoyingDependency> missingDependencies = new ArrayList<>();
        for (final AnnoyingDependency dependency : options.dependencies) {
            if (dependency.isNotInstalled() && missingDependencies.stream().noneMatch(dep -> dep.name.equals(dependency.name))) missingDependencies.add(dependency);
        }

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
     * Plugin enabling stuff
     *
     * @see #onEnable()
     */
    private void enablePlugin() {
        // Check if required dependencies are installed
        final Set<AnnoyingDependency> missing = options.dependencies.stream()
                .filter(dependency -> dependency.required && dependency.isNotInstalled())
                .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            final Set<String> missingNames = missing.stream()
                    .map(dependency -> dependency.name)
                    .collect(Collectors.toSet());
            log(Level.SEVERE, "&cDisabling " + getName() + " because it's missing required dependencies: &4" + StringUtils.join(missingNames, "&c, &4"));
            disablePlugin();
            return;
        }

        // Start messages
        final String name = getName() + " v" + getDescription().getVersion();
        final String authors = "By " + String.join(", ", getDescription().getAuthors());
        final String line = StringUtils.repeat("-", Math.max(name.length(), authors.length()));
        log(Level.INFO, options.colorDark + line);
        log(Level.INFO, options.colorLight + name);
        log(Level.INFO, options.colorLight + authors);
        log(Level.INFO, options.colorDark + line);

        // Register commands/events
        options.commands.forEach(AnnoyingCommand::register);
        options.listeners.forEach(AnnoyingListener::register);

        // Custom onEnable
        enable();
    }

    /**
     * Called when the plugin is disabled
     * <p>Do not try to override this method! Override {@link #disable()} instead
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
    @SuppressWarnings("EmptyMethod")
    public void load() {
        // This method is meant to be overridden
    }

    /**
     * Called after dependency checks, start-up messages, and command/listener registration.
     */
    @SuppressWarnings("EmptyMethod")
    public void enable() {
        // This method is meant to be overridden
    }

    /**
     * Called when the plugin is disabled
     */
    @SuppressWarnings("EmptyMethod")
    public void disable() {
        // This method is meant to be overridden
    }

    /**
     * Disables the plugin (not the API)
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public void disablePlugin() {
        // Unregister commands & listeners, cancel tasks, and disable the plugin
        options.commands.forEach(AnnoyingCommand::unregister);
        options.listeners.forEach(AnnoyingListener::unregister);
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    /**
     * Reloads the plugin (calls {@link PluginManager#disablePlugin(Plugin)} and then {@link PluginManager#enablePlugin(Plugin)})
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public void reloadPlugin() {
        final PluginManager manager = Bukkit.getPluginManager();
        manager.disablePlugin(this);
        manager.enablePlugin(this);
    }

    /**
     * Logs a message to the console
     *
     * @param   level   the level of the message
     * @param   message the message to log
     */
    public void log(@Nullable Level level, @NotNull String message) {
        if (level == null) level = Level.INFO;
        getLogger().log(level, AnnoyingUtility.color(message));
    }

    /**
     * Gets a string from {@link AnnoyingOptions#messagesFileName} with the specified key
     * <p>If the string is not found, it will return the key
     *
     * @param   key the key of the string
     *
     * @return      the string
     */
    @NotNull
    public String getMessagesString(@NotNull String key) {
        if (messages == null) return key;
        return messages.getString(key, key);
    }
}
