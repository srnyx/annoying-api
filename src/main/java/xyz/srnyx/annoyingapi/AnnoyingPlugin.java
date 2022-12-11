package xyz.srnyx.annoyingapi;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.dependency.AnnoyingDependency;
import xyz.srnyx.annoyingapi.dependency.AnnoyingDownload;
import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.plugin.ApiCommand;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;


/**
 * Represents a plugin using the API
 */
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * A {@link List} containing missing dependency names from <b>ALL</b> plugins using AnnoyingAPI
     */
    @NotNull private static final Set<String> MISSING_DEPENDENCIES = new HashSet<>();

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
     * Create your own constructor, <b>call {@code super()}</b>, and set your options ({@link #options})
     */
    public AnnoyingPlugin() {
        super();
        if (getName().equals("AnnoyingAPI")) {
            // API dependencies
            final Map<AnnoyingDownload.Platform, String> interface4 = new EnumMap<>(AnnoyingDownload.Platform.class);
            interface4.put(AnnoyingDownload.Platform.SPIGOT, "102119");
            options.dependencies.add(new AnnoyingDependency("Interface4", interface4, true, true));

            // API commands
            options.commands.add(new ApiCommand(this));
        }
    }

    @Override
    public final void onLoad() {
        messages = new AnnoyingResource(this, options.messagesFileName);
        options.prefix = AnnoyingUtility.getString(this, options.prefix);
        options.splitterJson = AnnoyingUtility.getString(this, options.splitterJson);
        options.splitterPlaceholder = AnnoyingUtility.getString(this, options.splitterPlaceholder);

        // Custom onLoad
        load();
    }

    /**
     * Called when the plugin is enabled.
     * <p>Do not override this method! Override {@link #enable()} instead
     *
     * @see #enable()
     */
    @Override
    public final void onEnable() {
        // Get missing dependencies
        final List<AnnoyingDependency> missingDependencies = new ArrayList<>();
        for (final AnnoyingDependency dependency : options.dependencies) {
            if (dependency.isNotInstalled() && MISSING_DEPENDENCIES.stream().noneMatch(name -> name.equals(dependency.name))) {
                missingDependencies.add(dependency);
                MISSING_DEPENDENCIES.add(dependency.name);
            }
        }

        // Download missing dependencies then enable the plugin
        if (!missingDependencies.isEmpty()) {
            log(Level.WARNING, "&6&lMissing dependencies! &eAnnoyingAPI will attempt to automatically download them...");
            new AnnoyingDownload(AnnoyingPlugin.this, missingDependencies).downloadPlugins(this::enablePlugin);
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
        for (final AnnoyingDependency dependency : options.dependencies) {
            if (dependency.required && dependency.isNotInstalled()) {
                log(Level.SEVERE, "&cMissing dependency, &4" + dependency.name + "&c is required! Unloading plugin...");
                unload();
                return;
            }
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
     * <p>Do not override this method! Override {@link #disable()} instead
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
     * Unloads the plugin
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public final void unload() {
        // Unregister commands listeners, cancel tasks, and disable the plugin
        options.commands.forEach(AnnoyingCommand::unregister);
        options.listeners.forEach(AnnoyingListener::unregister);
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getPluginManager().disablePlugin(this);

        // Close classloader
        final ClassLoader classLoader = getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            try {
                // plugin field
                final Field pluginField = classLoader.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(classLoader, null);

                // pluginInit field
                final Field pluginInitField = classLoader.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(classLoader, null);
            } catch (final IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }

            // Close URLClassLoader
            try {
                ((URLClassLoader) classLoader).close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        // Will not work on processes started with the -XX:+DisableExplicitGC flag
        // This tries to get around the issue where Windows refuses to unlock jar files that were previously loaded into the JVM
        System.gc();
    }

    /**
     * Reloads the plugin (calls {@link PluginManager#disablePlugin(Plugin)} and then {@link PluginManager#enablePlugin(Plugin)})
     * <p><i>This is not meant to be overriden, only override if you know what you're doing!</i>
     */
    public void reload() {
        final PluginManager manager = Bukkit.getPluginManager();
        manager.disablePlugin(this);
        manager.enablePlugin(this);
    }
}
