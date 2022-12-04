package xyz.srnyx.annoyingapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.srnyx.annoyingapi.download.AnnoyingDependency;
import xyz.srnyx.annoyingapi.download.AnnoyingDependencyException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Represents a plugin using the API
 */
public class AnnoyingPlugin extends JavaPlugin {
    /**
     * Options supplied by the plugin
     */
    public static AnnoyingOptions OPTIONS;
    /**
     * The logger used by the API
     */
    private static Logger LOGGER;
    /**
     * The plugin's data folder
     */
    public static File PLUGIN_FOLDER;

    /**
     * Creates a new instance of the plugin. This only exists to give the constructor a Javadoc.
     */
    public AnnoyingPlugin() {
        super();
    }

    /**
     * Called when this plugin is enabled
     */
    @Override
    public void onEnable() {
        try {
            onEnable(new AnnoyingOptions());
        } catch (final AnnoyingDependencyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Startup stuff for the API
     *
     * @param   options                     the options to use for the API
     *
     * @throws  AnnoyingDependencyException if a dependency is missing
     */
    public void onEnable(@NotNull AnnoyingOptions options) throws AnnoyingDependencyException {
        LOGGER = getLogger();
        PLUGIN_FOLDER = getDataFolder();
        OPTIONS = options;
        checkDependencies();

        // Start messages
        final String name = getName() + " v" + getDescription().getVersion();
        final String authors = "By " + String.join(", ", getDescription().getAuthors());
        final String line = "-".repeat(Math.max(name.length(), authors.length()));
        LOGGER.info(options.colorDark + line);
        LOGGER.info(options.colorLight + name);
        LOGGER.info(options.colorLight + authors);
        LOGGER.info(options.colorDark + line);
    }

    /**
     * Registers multiple {@link Listener}s
     *
     * @param   listeners   the listeners to register
     */
    public void registerListeners(Listener @NotNull ... listeners) {
        for (final Listener listener : listeners) Bukkit.getPluginManager().registerEvents(listener, this);
    }

    /**
     * Registers multiple {@link AnnoyingCommand}s
     *
     * @param   commands    the commands to register
     */
    public void registerCommands(AnnoyingCommand @NotNull ... commands) {
        for (final AnnoyingCommand command : commands) command.register(this);
    }

    /**
     * Checks if the dependencies are installed
     *
     * @throws  AnnoyingDependencyException if a dependency is missing
     */
    private void checkDependencies() throws AnnoyingDependencyException {
        for (final AnnoyingDependency dependency : OPTIONS.dependencies) if (!dependency.isInstalled()) throw new AnnoyingDependencyException(dependency);
    }

    /**
     * Logs a message to the console
     *
     * @param   level   the level of the message
     * @param   message the message to log
     */
    public static void log(@Nullable Level level, @NotNull String message) {
        if (level == null) level = Level.INFO;
        LOGGER.log(level, color(message));
    }

    /**
     * Translates {@code &} color codes to {@link ChatColor}
     *
     * @param   message the message to translate
     *
     * @return          the translated message
     */
    @NotNull @Contract("_ -> new")
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Formats a millisecond long using the given pattern
     *
     * @param   millis  the milliseconds to format
     * @param   pattern the pattern to use
     *
     * @return          the formatted time
     */
    @NotNull
    public static String formatMillis(long millis, @Nullable String pattern) {
        if (pattern == null) pattern = "mm:ss";
        return new SimpleDateFormat(pattern).format(millis);
    }
}
