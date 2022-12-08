package xyz.srnyx.annoyingapi;

import org.apache.commons.lang.StringUtils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingResource;
import xyz.srnyx.annoyingapi.plugin.ApiCommand;

import java.util.*;
import java.util.logging.Level;


/**
 * Represents a plugin using the API
 */
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * A {@link List} containing missing dependencies from <b>ALL</b> plugins using AnnoyingAPI
     */
    @NotNull private static final List<AnnoyingDependency> missingDependencies = new ArrayList<>();

    /**
     * Instance of {@link AnnoyingCommandRegister} to register commands
     */
    @Nullable public static AnnoyingCommandRegister commandRegister = null;

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
            // Command register
            try {
                Class.forName("com.mojang.brigadier.CommandDispatcher");
                commandRegister = new AnnoyingCommandRegister();
            } catch (final ClassNotFoundException | NoClassDefFoundError ignored) {
                // Ignored
            }

            // Get API dependencies
            final Map<AnnoyingDownload.Platform, String> interface4 = new EnumMap<>(AnnoyingDownload.Platform.class);
            interface4.put(AnnoyingDownload.Platform.SPIGOT, "102119");
            options.dependencies.add(new AnnoyingDependency("Interface4", interface4, true));

            // API commands
            options.commands.add(new ApiCommand(this));
        }
    }

    /**
     * Called when the plugin is enabled.
     * <p>Do not override this method! Override {@link #enable()} instead
     *
     * @see #enable()
     */
    @Override
    public final void onEnable() {
        messages = new AnnoyingResource(this, options.messagesFileName);
        options.prefix = AnnoyingUtility.getString(this, options.prefix);
        options.splitterJson = AnnoyingUtility.getString(this, options.splitterJson);
        options.splitterPlaceholder = AnnoyingUtility.getString(this, options.splitterPlaceholder);

        // Get missing dependencies
        for (final AnnoyingDependency dependency : options.dependencies) {
            if (dependency.isNotInstalled() && missingDependencies.stream().noneMatch(d -> d.name.equals(dependency.name))) missingDependencies.add(dependency);
        }

        // Download missing dependencies using API
        if (getName().equals("AnnoyingAPI")) {
            new BukkitRunnable() {
                public void run() {
                    if (!missingDependencies.isEmpty()) {
                        log(Level.WARNING, "&6&lMissing dependencies! &eAnnoyingAPI will attempt to automatically download them...");
                        new AnnoyingDownload(AnnoyingPlugin.this, missingDependencies).downloadPlugins();
                    }
                }
            }.runTaskLater(this, 1L);
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
}
