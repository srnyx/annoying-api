package xyz.srnyx.annoyingapi;

import org.apache.commons.lang.StringUtils;

import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.file.AnnoyingResource;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


/**
 * Represents a plugin using the API
 */
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * The API options for the plugin
     */
    @NotNull public final AnnoyingOptions options = new AnnoyingOptions();

    /**
     * The {@link AnnoyingResource} that contains the plugin's messages
     */
    @NotNull public AnnoyingResource messages;

    /**
     * Stores the cooldowns for each player/type
     */
    @NotNull public final Map<UUID, Map<AnnoyingCooldown.CooldownType, Long>> cooldowns = new HashMap<>();

    /**
     * Initializes the API, plugin, and messages
     * <p>Create your own constructor, call {@code super()}, and set your options ({@link #options})
     */
    public AnnoyingPlugin() {
        super();
        messages = new AnnoyingResource(this, options.messagesFileName);
        options.prefix = AnnoyingUtility.getString(this, options.prefix);
        options.splitterJson = AnnoyingUtility.getString(this, options.splitterJson);
        options.splitterPlaceholder = AnnoyingUtility.getString(this, options.splitterPlaceholder);
    }

    /**
     * Called when the plugin is enabled.
     * <p>Do not override this method! Override {@link #enable()} instead
     *
     * @see #enable()
     */
    @Override
    public final void onEnable() {
        // Get dependencies
        final Map<AnnoyingDownload.Platform, String> interface4 = new EnumMap<>(AnnoyingDownload.Platform.class);
        interface4.put(AnnoyingDownload.Platform.SPIGOT, "102119");
        options.dependencies.add(new AnnoyingDependency("Interface4", interface4));

        // Download dependencies
        final Set<AnnoyingDependency> missing = options.dependencies.stream()
                .filter(dependency -> !dependency.isInstalled())
                .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            log(Level.WARNING, "&6&lMissing dependencies! &eAnnoyingAPI will attempt to automatically download them...");
            new AnnoyingDownload(this, missing).downloadPlugins(options.dependencyFinishTask);
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
