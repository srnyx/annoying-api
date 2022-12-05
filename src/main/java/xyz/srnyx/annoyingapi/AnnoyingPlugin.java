package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.cooldown.AnnoyingCooldownType;
import xyz.srnyx.annoyingapi.download.AnnoyingDependency;
import xyz.srnyx.annoyingapi.download.AnnoyingDownload;
import xyz.srnyx.annoyingapi.download.AnnoyingDownloadFinish;
import xyz.srnyx.annoyingapi.download.AnnoyingPlatform;

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
    @NotNull public AnnoyingOptions options = new AnnoyingOptions();
    /**
     * Stores the cooldowns for each player/type
     */
    @NotNull public final Map<UUID, Map<AnnoyingCooldownType, Long>> cooldowns = new HashMap<>();

    /**
     * Creates a new instance of the plugin. This only exists to give the constructor a Javadoc.
     */
    public AnnoyingPlugin() {
        super();
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
        final Set<AnnoyingDependency> dependencies = new HashSet<>(getDependencies());
        final Map<AnnoyingPlatform, String> interface4 = new EnumMap<>(AnnoyingPlatform.class);
        interface4.put(AnnoyingPlatform.SPIGOT, "102119");
        dependencies.add(new AnnoyingDependency("Interface4", interface4));

        // Download dependencies
        final Set<AnnoyingDependency> missing = dependencies.stream()
                .filter(dependency -> !dependency.isInstalled())
                .collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            log(Level.WARNING, "&6&lMissing dependencies! &eAnnoyingAPI will attempt to automatically download them...");
            new AnnoyingDownload(this, missing).downloadPlugins(getDependencyFinish());
            return;
        }
        options = getOptions();

        // Start messages
        final String name = getName() + " v" + getDescription().getVersion();
        final String authors = "By " + String.join(", ", getDescription().getAuthors());
        final String line = String.join("", Collections.nCopies(Math.max(name.length(), authors.length()), "-"));
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
     * Override this method to set plugin/API options
     *
     * @return  the API options for the plugin
     */
    @NotNull
    public AnnoyingOptions getOptions() {
        return new AnnoyingOptions();
    }

    /**
     * Dependencies of the API and the plugin
     *
     * @return  the dependencies of the plugin
     */
    @NotNull
    public Set<AnnoyingDependency> getDependencies() {
        return new HashSet<>();
    }

    /**
     * The task to run when all dependencies are downloaded
     *
     * @return  the task to run when all dependencies are downloaded
     */
    @NotNull
    public AnnoyingDownloadFinish getDependencyFinish() {
        return plugins -> Bukkit.getServer().shutdown();
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
